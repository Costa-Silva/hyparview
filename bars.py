#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Oct 30 09:43:31 2017

@author: rafaelseara
"""
import json
import pygal
import matplotlib.pyplot as plt
from PIL import Image 
import time
import matplotlib.animation as animation


fig = plt.figure()
#ax1.grid(False)

def animate(i):
    data_file =  open('data.json')
    data = json.load(data_file)
    
    graphNodesAv = []
    graphNodesPv = []
    messageCounters = []
    peersId = []
    
    
    for n in range(0,len(data["data"])):
        for i in range(0,len(data["data"][n]["av"])):
            graphNodesAv.append((data["data"][n]["id"][:1], data["data"][n]["av"][i][:1]))
        for k in range(0,len(data["data"][n]["pv"])):
            graphNodesPv.append((data["data"][n]["id"][:1], data["data"][n]["pv"][k][:1]))
        a =[]
        a.append(data["data"][n]["JoinsReceived"])
        a.append(data["data"][n]["ForwardJoinsReceived"])
        a.append(data["data"][n]["ForwardJoinsSent"])
        #a.append(data["data"][n]["NeighborRequestsReceived"])
        #a.append(data["data"][n]["NeighborRequestsSent"])
        a.append(data["data"][n]["DisconnectsReceived"])
        a.append(data["data"][n]["DisconnectsSent"])
        a.append(data["data"][n]["ShufflesReceived"])
        a.append(data["data"][n]["ShufflesSent"])
        a.append(data["data"][n]["BroadcastsSent"])
        a.append(data["data"][n]["BroadcastsReceived"])
        a.append(data["data"][n]["ResolveConflicts"])
        a.append(data["data"][n]["ChecksIfAlive"])


        messageCounters.append(a)
        peersId.append((data["data"][n]["id"]))
    
    line_chart = pygal.Bar(legend_at_bottom=True)
    line_chart.title = 'Message usage evolution'
    line_chart.x_labels = map(str, peersId)
    
    
    JoinsReceived = []
    ForwardJoinsReceived = []
    ForwardJoinsSent = []
    #NeighborRequestsReceived = []
    #NeighborRequestsSent = []
    DisconnectsReceived = []
    DisconnectsSent = []
    ShufflesReceived = []
    ShufflesSent = []
    BroadcastsSent = []
    BroadcastsReceived = []
    ResolveConflicts = []
    ChecksIfAlive = []

    
    for m in messageCounters:
        JoinsReceived.append(m[0])
        ForwardJoinsReceived.append(m[1])
        ForwardJoinsSent.append(m[2])
        #NeighborRequestsReceived.append(m[3])
        #NeighborRequestsSent.append(m[4])
        DisconnectsReceived.append(m[3])
        DisconnectsSent.append(m[4])
        ShufflesReceived.append(m[5])
        ShufflesSent.append(m[6])
        BroadcastsSent.append(m[7])
        BroadcastsReceived.append(m[8])
        ResolveConflicts.append(m[9])
        ChecksIfAlive.append(m[10])
    
    line_chart.add('JoinsReceived', JoinsReceived)
    line_chart.add( 'ForwardJoinsReceived', ForwardJoinsReceived)
    line_chart.add( 'ForwardJoinsSent', ForwardJoinsSent)
    #line_chart.add( 'NeighborRequestsReceived', NeighborRequestsReceived)
    #line_chart.add( 'NeighborRequestsSent', NeighborRequestsSent)
    line_chart.add( 'DisconnectsReceived', DisconnectsReceived)
    line_chart.add( 'DisconnectsSent', DisconnectsSent)
    line_chart.add( 'ShufflesReceived', ShufflesReceived)
    line_chart.add( 'ShufflesSent', ShufflesSent)
    line_chart.add( 'BroadcastsSent', BroadcastsSent)
    line_chart.add( 'BroadcastsReceived', BroadcastsReceived)
    line_chart.add( 'ResolveConflicts', ResolveConflicts)
    line_chart.add( 'ChecksIfAlive', ChecksIfAlive)

    
    line_chart.render_to_png(filename="plot.png")
    #img = Image.imread("plot.png")
    img =Image.open("plot.png")
    plt.imshow(img)

ani = animation.FuncAnimation(fig, animate, interval=3000)
plt.show()

    

