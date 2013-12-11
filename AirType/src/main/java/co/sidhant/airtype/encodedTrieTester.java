package co.sidhant.airtype;


import java.lang.String;import java.lang.System;import java.util.BitSet;
import java.util.Random;

/**
 * Created by sid9102 on 12/10/13.
 *
 * For intensive testing of the encoded trie methods
 */
public class EncodedTrieTester
{
    public static void main(String[] args)
    {
        BitSet trieBits = new BitSet();

        Random rand = new Random();

        int bitSize = 20000;

        for(int i = 0; i < bitSize; i++)
        {
            if(rand.nextBoolean())
            {
                trieBits.set(i);
            }
        }

        BitSet rankDir = generateRankDirectory(trieBits);

        for(int i = 0; i < bitSize; i++)
        {
            if(i % 25 == 0)
            {
                int rank = (int) rank(i, trieBits, rankDir);
                if(getRankNaive(trieBits, i) != rank)
                {
                    System.out.println("rank mismatch");
                }

                int naiveSelect = selectNaive(trieBits, rank);
                int properSelect = select(rank, trieBits, rankDir);

                if(naiveSelect != properSelect)
                {
                    System.out.println("select mismatch, expected " + naiveSelect + ", got " + properSelect);
                }
            }
        }
    }

    private static int getRankNaive(BitSet bitSet, int index)
    {
        int rank = 0;
        for(int i = 0; i <= index; i++)
        {
            if(bitSet.get(i))
            {
                rank++;
            }
        }
        return rank;
    }

    private static int selectNaive(BitSet bitSet, int index)
    {
        if(index == 0)
            return 1;
        int count = 0;
        for(int i = 0; i < bitSet.length(); i++)
        {
            if(bitSet.get(i))
            {
                count++;
            }
            if(count == index)
                return i;
        }
        return -1;
    }

    private static int findFirstOccurrence(long[] longs, long value)
    {
        for(int i = 0; i < longs.length; i++)
        {
            if(longs[i] == value)
            {
                return i;
            }
        }

        return -1;
    }

    // Returns the i-th occurrence of a 1 in the BitSet, by binary searching the rank directory rankDirectory
    private static int select(int i, BitSet bitSet, BitSet rankDirectory)
    {
        if(i == 0)
            return 1;
        // Start at the middle of the bitset
        int upperLimit = bitSet.length();
        int searchIndex = upperLimit / 2;
        int oldSearchIndex = 0;
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

            if(oldSearchIndex == searchIndex)
                break;

            // Prevent infinite searching
            if(searchIndex > bitSet.length())
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

    // Generate a rank directory for the given BitSet
    // A rank directory is a directory of the number of 1's until that point in a BitSet
    // useful for constant time searching of a byte array
    private static BitSet generateRankDirectory(BitSet bitSet)
    {
        BitSet rankDir = new BitSet();
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
            BitSet dataBits = new BitSet();
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

            BitSet headerBits = valueOf(header);

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

    // Returns a bitset from a provided long[]
    private static BitSet valueOf(long[] longs)
    {
        BitSet result = new BitSet();
        for(int i = 0; i < longs.length; i++)
        {
            long bit = 1;
            for(int j = 0; j < 64; j++)
            {
                // Check the bit at j
                if((bit & longs[i]) != 0)
                {
                    result.set(j + (i * 64));
                }
                bit <<= 1;
            }
        }
        return result;
    }
    // Produces a long[] from a given bitset
    private static long[] bitSetToLong(BitSet bitSet)
    {
        // figure out how many longs we need, size will always be some multiple of 64
        int size = bitSet.size() / 64;
        if(size == 0)
            size++;
        long[] result = new long[size];
        for(int i = 0; i < size; i++)
        {
            long bit = 1;
            for(int j = 0; j < 64; j++)
            {
                if(bitSet.get(j + (i * 64)))
                {
                    result[i] = result[i] | bit;
                }
                bit <<= 1;
            }
        }
        return result;
    }

    // Produces an int[] from a given superblock of the rank counts contained by the blocks
    private static int[] superBlockCounts(BitSet superBlock)
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

    private static long rank(int index, BitSet bitSet, BitSet rankDirectory)
    {
        long result = 0;

        // First figure out which superblock this index belongs to.
        // Each superblock represents 2048 bits
        int superBlockIndex = index / 2048;

        // Next, figure out which data block it belongs to.
        // Each data block represents 256 bits
        int dataBlockIndex = (index % 2048) / 256;
        // Finally, figure out the index within that data block.
        int bitIndex = (index % 2048) % 256;

        // Get the superblock, then get the rank of the header
        BitSet superBlock = rankDirectory.get(superBlockIndex * 128, (superBlockIndex + 1) * 128);
        long[] header = bitSetToLong(superBlock);
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

}
