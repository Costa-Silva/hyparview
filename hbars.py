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
    
    graphNodesGv = []
    peersId = []

    line_chart = pygal.HorizontalBar(legend_at_bottom=True)
    
    for n in range(0,len(data["data"])):
        strGV = (data["data"][n]["id"]) + " = "
        for i in range(0,len(data["data"][n]["gv"])):
            graphNodesGv.append((data["data"][n]["id"][:1], data["data"][n]["gv"][i][:1]))
            strGV = strGV + data["data"][n]["gv"][i][:1] + " "

        line_chart.add(strGV, len(graphNodesGv))
        graphNodesGv = []
    

    
    line_chart.render_to_png(filename="plotGV.png")
    #img = Image.imread("plot.png")
    img =Image.open("plotGV.png")
    plt.imshow(img)

ani = animation.FuncAnimation(fig, animate, interval=3000)
plt.show()

    

