import matplotlib.pyplot as plt
import yaml
import json
import re
import ast

def parseList(s):
    s = s.strip().lstrip('[').rstrip(']')
    arr = list(map(int, s.split(',')))
    return arr

def parseDictList(s):
    pat = r'[a-z]+[0-9]+'
    match = re.findall(pat, s)

    for m in set(match):
        s = s.replace(m, f"'{m}'")

    s = s.replace("=", ":")

    return ast.literal_eval(s)

def parseDict(s):
    s = s.replace("=", ":")

    return ast.literal_eval(s)

def makeHist(d):
    hist = {}
    Groups = ['student', 'professor', 'barista']

    for k in d:
        hist[k] = {}
        for g in Groups:
            ans = 0
            for v in d[k]:
                if g in v: ans += 1
            hist[k][g] = ans

    return hist

def parseDic(s, sep='='):
    return None

def parseReport(file_name = "../reports/FMI_RumourAppReporter.txt"):
    d = {}

    with open(file_name, "r") as f:
        txt = f.read().strip().split('\n')

    for line in txt:
        key, val = line.split(':')
        key = key.strip()
        val = val.strip()

        d[key] = val

    # print(d)

    rumours = parseList(d['RumourList'])
    received = parseDictList(d['Received'])
    infected = parseDictList(d['Infected'])
    hop_count = parseDict(d['HopCount'])

    print(rumours)
    print(received)
    print(infected)
    print(hop_count)

    return rumours, received, infected, hop_count

if __name__ == "__main__":

    d = {}

    with open("../reports/FMI_RumourAppReporter.txt", "r") as f:
        txt = f.read().strip().split('\n')

        for line in txt:
            key, val = line.split(':')
            key = key.strip()
            val = val.strip()

            d[key] = val

    print(d)

    rumours = parseList(d['RumourList'])
    received = parseDictList(d['Received'])
    infected = parseDictList(d['Infected'])
    hop_count = parseDict(d['HopCount'])

    print(rumours)
    print(received)
    print(infected)
    print(hop_count)

    hist_received = makeHist(received)
    hist_infected = makeHist(infected)

    print(hist_received)
    print(hist_infected)







    # js = json.loads(txt)

    # print(js)
