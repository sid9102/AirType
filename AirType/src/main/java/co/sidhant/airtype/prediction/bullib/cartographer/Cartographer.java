package bullib.cartographer;

import bullib.network.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.io.*;

import javax.swing.*;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

@SuppressWarnings({"serial", "rawtypes","unchecked"})
public class Cartographer<T extends Serializable> extends JFrame {
    private Color color;
    private Dimension size;
	private JGraphModelAdapter jgAdapter;
	private ListenableGraph graph;

    public Cartographer(Network<T> network, int width, int height, int limit, int settled) throws Exception{
        init(network, width, height, limit, settled);
        setVisible(true);
    }

	public void init(Network<T> network, int width, int height, int limit, int settled) throws Exception {

        // setup graph
        color = Color.decode("#FAFBFF");
        size = new Dimension(width, height);
        graph = new ListenableDirectedGraph(DefaultWeightedEdge.class);
        jgAdapter = new JGraphModelAdapter(graph);
        JGraph jgraph = new JGraph(jgAdapter);

        // setup display
        JPanel container = new JPanel();
        JScrollPane scrPane = new JScrollPane(container);
        add(scrPane);
        adjustDisplaySettings(jgraph);
        container.add(jgraph);

        // draw the graph
        drawGraph(network, graph, width, height, limit, settled);
        setVisible(true);
    }

	public void drawGraph(Network<T> network, ListenableGraph graph, int width, int height, int limit, int settled) throws Exception {
    	
        // Add vertices
        HashMap<T, GraphCoordinate> coordinates = new HashMap<T, GraphCoordinate>();
        Random placement = new Random();
        for(T node : network.getNodes()){
        	graph.addVertex(node);
        	coordinates.put(node, new GraphCoordinate((int)(placement.nextDouble() * width), (int)(placement.nextDouble() * height)));
        }
        
        // Add edges
        for(T parent : network.getNodes()){
        	for(Entry<T, Weight> link : network.getOutgoingLinks(parent)){
        		graph.addEdge(parent, link.getKey(), link.getValue());
        	}
        }

        // Simulate until an equilibrium is reached
        int movlim = coordinates.keySet().size() / 10;
        GraphCoordinate rootcoord = coordinates.get(network.root);
        rootcoord.x = width/2;
        rootcoord.y = height/2;
        int cycles = 0;
        int movement = Integer.MAX_VALUE;
        GraphCoordinate ac;
        GraphCoordinate bc;
        while(cycles++ < limit && movement > movlim){
        	movement = 0;
        	
        	for(T an : network.getNodes()){
        		ac = coordinates.get(an);
        		for(T bn : network.getNodes()) if(an != bn){
        			bc = coordinates.get(bn);
        			ac.affect(bc, network.contains(an,bn));
        		}
        	}
        	
        	for(GraphCoordinate c : coordinates.values()){
        		movement += c.collect(1);
        	}
        	
            rootcoord.x = width/2;
            rootcoord.y = height/2;
        }

        // Draw the graph
        for(Entry<T, GraphCoordinate> nac : coordinates.entrySet()){
        	positionVertexAt(nac.getKey(), nac.getValue().x, nac.getValue().y);
        }
    }

    private void positionVertexAt(Object vertex, int x, int y) { 
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);
        GraphConstants.setBounds(attr, new Rectangle2D.Double(x,y,bounds.getWidth(),bounds.getHeight()));
        org.jgraph.graph.AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }

    private void adjustDisplaySettings(JGraph jg) {
        jg.setPreferredSize(size);
        jg.setBackground(color);
    }
}

class GraphCoordinate {
    public int xf;
    public int yf;

    public int x;
    public int y;

    public GraphCoordinate(int x, int y){
        this.x = x;
        this.y = y;
        xf = 0;
        yf = 0;
    }
    
    public void affect( GraphCoordinate other, boolean connected){
        int xd = other.x - this.x;
        int yd = other.y - this.y;
        int d = (int)Math.sqrt(xd * xd + yd * yd);
        int s = Math.abs(xd) + Math.abs(yd);
        int f = 0;
    	if(connected){
    		f = -30 * 30 / d - 30;
    	}
    	else{
    		f = 10 * 10 / d - 10;
    	}
        
        other.xf += f * xd / s;
        other.yf += f * yd / s;
    }

    public int collect(int signifigant){
        x += xf;
        y += yf;

        if(Math.sqrt(xf*xf + yf*yf) < (double)signifigant){
            xf = 0;
            yf = 0;
            return 0;
        }
        
        xf = 0;
        yf = 0;
        return 1;
    }
}