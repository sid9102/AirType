package io.airtype;

import org.apache.lucene.util.OpenBitSet;

/**
 * Created by sid9102 on 1/26/14.
 *
 * Succinct encoding utility functions
 */
public class SuccinctEncoding
{
    // Generate a rank directory for the given BitSet
    // A rank directory is a directory of the number of 1's until that point in a BitSet
    // useful for constant time searching of a byte array
    public static OpenBitSet generateRankDirectory(OpenBitSet bitSet)
    {
        OpenBitSet rankDir = new OpenBitSet();
        int superBlockIndex = 0;
        long rank = 0;

        // We're allocating 64 bits for a superblock header,
        // and then another 64, for 7 9-bit data blocks. A superblock is therefore 128 bits in size.
        // This means that each data block is a jump of 256 bits in the input BitSet,
        // and each superblock is (1 + 7) * 256 bits, or 2048 bits
        int i = 0;
        while(i < bitSet.length())
        {
            // Superblock header
            long[] header = new long[1];
            header[0] = rank;
            // This bitset maintains the bits for the 7 data blocks
            OpenBitSet dataBits = new OpenBitSet();
            // This loop runs once per data block
            for(int j = 0; j < 7; j++)
            {
                long oldValue = rank;
                // This loop updates the rank 256 times, traversing the size of a data block
                for(int k = 0; k < 256; k++)
                {
                    if(bitSet.get(i))
                    {
                        rank++;
                    }
                    i++;
                }
                // Encode each data block
                int block = (int) (rank - oldValue);
                int bit = 1;
                for(int k = 0; k < 9; k++)
                {
                    if((bit & block) != 0)
                    {
                        dataBits.set(k + (j * 9));
                    }
                    bit <<= 1;
                }
            }

            OpenBitSet headerBits = new OpenBitSet(header, header.length);

            // Insert this superblock into the rank directory
            for(int j = 0; j < 128; j++)
            {
                if(j < 64)
                {
                    if(headerBits.get(j))
                    {
                        rankDir.set(j + (superBlockIndex * 128));
                    }
                }
                else
                {
                    if(dataBits.get(j - 64))
                    {
                        rankDir.set(j + (superBlockIndex * 128));
                    }
                }
            }

            // Update rank for the next superblock header
            for(int k = 0; k < 256; k++)
            {
                if(bitSet.get(i))
                {
                    rank++;
                }
                i++;
            }
            superBlockIndex++;
        }

        return rankDir;
    }

    // Returns the rank at a particular index in the BitSet bitSet, using the rank directory rankDirectory
    public static long rank(int index, OpenBitSet bitSet, OpenBitSet rankDirectory)
    {
        long result;
        if(index == 0)
            return 1;
        // First figure out which superblock this index belongs to.
        // Each superblock represents 2048 bits
        int superBlockIndex = index / 2048;

        // Next, figure out which data block it belongs to.
        // Each data block represents 256 bits
        int dataBlockIndex = (index % 2048) / 256;

        // Get the superblock, then get the rank of the header
        OpenBitSet superBlock = getSubSet(rankDirectory, superBlockIndex * 128, (superBlockIndex + 1) * 128);
        long[] header = superBlock.getBits();
        result = header[0];

        // Next calculate the rank until the data block.
        int[] dataBlocks = superBlockCounts(superBlock);
        for(int i = 0; i < dataBlockIndex; i++)
        {
            result += dataBlocks[i];
        }

        // Now calculate the rank within the data block.
        // This int is the index of the first bit in the data block to which the bit's index belongs.
        int dataBlockBitIndex = (superBlockIndex * 2048) + (dataBlockIndex * 256);
        for(int i = dataBlockBitIndex; i <= index; i++)
        {
            if(bitSet.get(i))
            {
                result++;
            }
        }

        // There we go, constant time rank computation!
        // The first lookup takes one rankDirectory.get call, the second takes 7 iterations of a loop
        // to get the data blocks and worst case 7 to add them all together, and then finally
        // worst case 256 bits are counted to get a rank.

        return result;
    }

    // Produces an int[] from a given superblock of the rank counts contained by the blocks
    private static int[] superBlockCounts(OpenBitSet superBlock)
    {
        int[] result = new int[7];
        for(int i = 0; i < 7; i++)
        {
            int bit = 1;
            int count = 0;
            for(int j = 0; j < 9; j++)
            {
                if(superBlock.get(64 + j + (i * 9)))
                {
                    count = count | bit;
                }
                bit <<= 1;
            }
            result[i] = count;
        }
        return result;
    }

    // Returns the i-th occurrence of a 1 in the BitSet, by binary searching the rank directory rankDirectory
    public static int select(int i, OpenBitSet bitSet, OpenBitSet rankDirectory)
    {
        // Start at the middle of the bitset
        int upperLimit = bitSet.length();
        int searchIndex = upperLimit / 2;
        int oldSearchIndex;
        int rank = 0;
        // Binary search!
        while(rank != i)
        {
            oldSearchIndex = searchIndex;
            rank = (int) rank(searchIndex, bitSet, rankDirectory);
            if(rank < i)
            {
                searchIndex += (upperLimit - searchIndex)/ 2;
            }
            else if(rank > i)
            {
                upperLimit = searchIndex;
                searchIndex /= 2;
            }

            // Prevent infinite searching
            if(searchIndex > bitSet.length() || (oldSearchIndex == searchIndex && rank != i))
                return -1;
        }

        // Now find the actual beginning of this word, search backwards until rank changes
        while(rank == i)
        {
            searchIndex--;
            rank = (int) rank(searchIndex, bitSet, rankDirectory);
        }
        searchIndex++;
        return searchIndex;
    }

    public static OpenBitSet getSubSet(OpenBitSet bitset, int begin, int end)
    {
        OpenBitSet result = new OpenBitSet();
        for(int i  = begin; i < end; i++)
        {
            if(bitset.get(i))
                result.set((long) i - begin);
        }
        return result;
    }



    // Get a word for a provided BitSet, encoded according to my character encoding scheme
    public static String getWord(OpenBitSet word)
    {
        // The curWord may have trailing 0s, we need to make sure
        // to read until the end of the word,including trailing zeroes.
        String result = "";
        for(int i = 1; i < word.length() + 1; i++)
        {
            int bit = 1;
            int curLetter = 0;
            int curStart = i * 5;
            for(int j = curStart; j < curStart + 5; j++)
            {
                if(word.get(j))
                {
                    curLetter |=  bit;
                }
                bit <<= 1;
            }
            if(curLetter == 0)
            {
                char letter = '\'';
                result += letter;
            }
            else if(curLetter >= 27 || curLetter < 0)
                return result;
            else
            {
                curLetter += 0x60;
                char letter = (char) curLetter;
                result += letter;
            }
        }
        return result;
    }
}
