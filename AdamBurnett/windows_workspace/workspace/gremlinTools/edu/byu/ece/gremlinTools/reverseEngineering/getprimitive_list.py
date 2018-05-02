#! /usr/bin/python
import os
import sys

'''
Created on May 5, 2010

@author: gumbie
'''
p =open("xc5vlx30.xdlrc",'r')
Ou=open("primitive_List.txt",'w');
i=0
for line in p:
    if 'primitive_def' in line: 
    	if (i==0):
    	    i=1
    	else:
    		Ou.write(line)

p.close()
Ou.close()

            