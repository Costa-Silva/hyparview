#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Oct 21 04:06:12 2017

@author: rafaelseara
"""
import json
import networkx as nx
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib import style
import time

#style.use('fivethirtyeight')

fig = plt.figure()
#ax1 = fig.add_subplot(111, polar=True)
#ax1.grid(False)

def animate(i):
    plt.clf()
    data_file =  open('data.json')
    data = json.load(data_file)
    
    graphNodesAv = []
    graphNodesPv = []


    for n in range(0,len(data["data"])):
        for i in range(0,len(data["data"][n]["av"])):
            graphNodesAv.append((data["data"][n]["id"][:1], data["data"][n]["av"][i][:1]))
        for k in range(0,len(data["data"][n]["pv"])):
            graphNodesPv.append((data["data"][n]["id"][:1], data["data"][n]["pv"][k][:1]))
    
        
    G = nx.DiGraph()
    
    G.add_edges_from(graphNodesAv + graphNodesPv)
    
    val_map = {'0': 1.0,
               '1': 0.95,
               '2': 0.90,
               '3': 0.85,
               '4': 0.80,
               '5': 0.75,
               '6': 0.70,
               '7': 0.65,
               '8': 0.60,
               '9': 0.55,
               '10': 0.50,
               '11': 0.45,
               '12': 0.40,
               '13': 0.35,
               '14': 0.30,
               '15': 0.25,
               '16': 0.20,
               '17': 0.15,
               '18': 0.10,
               '19': 0.5,
               '20': 0.0}
    
    values = [val_map.get(node, 0.25) for node in G.nodes()]
    
    
    # Specify the edges you want here
    red_edges = graphNodesAv
    
    edge_colours = ['black' if not edge in red_edges else 'red'
                    for edge in G.edges()]
    black_edges = [edge for edge in G.edges() if edge not in red_edges]
    
    # Need to create a layout when doing
    # separate calls to draw nodes and edges
    #pos = nx.spring_layout(G)
    pos = nx.circular_layout(G)

    nx.draw_networkx_nodes(G, pos, cmap=plt.get_cmap('jet'), 
                           node_color = values, node_size = 500)
    nx.draw_networkx_labels(G, pos)
    nx.draw_networkx_edges(G, pos, edgelist=red_edges, edge_color='r', arrows=True)
    nx.draw_networkx_edges(G, pos, edgelist=black_edges, arrows=False)
    #plt.show()
    print("hello")

    
ani = animation.FuncAnimation(fig, animate, interval=3000)
plt.show()
print("hello1")
