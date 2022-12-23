import pandas as pd
import matplotlib.cm as cm
import numpy as np
import matplotlib.pyplot as plt
from report import parseReport, makeHist
from glob import glob

def plot_clustered_stacked(dfall, labels=None, title="Spread of rumours for within each group",  H="/", **kwargs):
    """Given a list of dataframes, with identical columns and index, create a clustered stacked bar plot.
labels is a list of the names of the dataframe, used for the legend
title is a string for the title of the plot
H is the hatch used for identification of the different dataframe"""

    n_df = len(dfall)
    n_col = len(dfall[0].columns)
    n_ind = len(dfall[0].index)
    axe = plt.subplot(111)

    for df in dfall : # for each data frame
        axe = df.plot(kind="bar",
                      linewidth=0,
                      stacked=True,
                      ax=axe,
                      legend=False,
                      grid=False,
                      **kwargs)  # make bar plots

    h,l = axe.get_legend_handles_labels() # get the handles we want to modify
    for i in range(0, n_df * n_col, n_col): # len(h) = n_col * n_df
        for j, pa in enumerate(h[i:i+n_col]):
            for rect in pa.patches: # for each index
                rect.set_x(rect.get_x() + 1 / float(n_df + 1) * i / float(n_col))
                rect.set_hatch(H * int(i / n_col)) #edited part
                rect.set_width(1 / float(n_df + 1))

    axe.set_xticks((np.arange(0, 2 * n_ind, 2) + 1 / float(n_df + 1)) / 2.)
    axe.set_xticklabels(df.index, rotation = 0)
    axe.set_title(title)
    axe.set_xlabel('Rumour ID')
    axe.set_ylabel('Frequency')

    # Add invisible data to add another legend
    n=[]
    for i in range(n_df):
        n.append(axe.bar(0, 0, color="gray", hatch=H * i))

    l1 = axe.legend(h[:n_col], l[:n_col], loc=[1.01, 0.5])
    if labels is not None:
        l2 = plt.legend(n, labels, loc=[1.01, 0.1])
    axe.add_artist(l1)
    axe.set_yscale('log')
    return axe

if __name__ == "__main__":

    Groups = ['professor', 'student', 'cleaner', 'barista', 'visitor']

    files = list(map(lambda s:s.replace("\\", "/"), glob("../reports/*RumourAppReporter.txt")))

    print(files)
    SHOW = False

    for file in files:
        print(file)

        rumours, received, infected, hop_count = parseReport(file)

        hist_received = makeHist(received)
        hist_infected = makeHist(infected)

        tot_received = {i : sum(hist_received[i].values()) for i in hist_received}
        tot_infected = {i : sum(hist_infected[i].values()) for i in hist_infected}

        tot_received_ = sum(tot_received.values())
        tot_infected_ = sum(tot_infected.values())

        print(rumours)
        print(hist_received)
        print(hist_infected)
        print(tot_received)
        print(tot_infected)
        print(tot_received_)
        print(tot_infected_)
        print(hop_count)

        df = []

        for id in received:
            arr = []
            for g in Groups:
                rec = hist_received[id][g] if g in hist_received[id] else 0
                inf = hist_infected[id][g] if (id in hist_infected and g in hist_infected[id]) else 0

                n_inf = rec - inf

                arr.append([inf, n_inf])

            df.append(arr)

        df = np.array(df)

        print(df.shape)

        ids = received.keys()
        ids = [Groups[id-1] for id in ids]

        df_plot = []

        for i in range(len(Groups)):
            df_i = df[:, i, :]
            df_i = pd.DataFrame(df_i, index=ids, columns=["infected", "non-infected"])
            df_plot.append(df_i)

        # print(df_plot)
        fig = plt.figure()

        plot_clustered_stacked(df_plot,Groups)

        out_name = file.split("/")[-1].strip(".txt")
        out_name = "../plots/" + out_name + ".png"
        print(out_name)
        plt.savefig(out_name, bbox_inches = 'tight')

        if SHOW:
            plt.show()

        plt.close()
