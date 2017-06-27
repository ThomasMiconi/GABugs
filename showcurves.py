import matplotlib.pyplot as plt
import numpy as np
import glob

#strs = glob.glob('results_sensewhichhalf_nodirectio*_SEED0.txt')
strs = glob.glob('results_fullsens_*_SEED0.txt') + glob.glob('results_scoresens_*_SEED0.txt')

plt.ion()
plt.clf()
for numstr in range (len(strs)):
    fnames = glob.glob(strs[numstr][:-6]+"*");
    allresults = []
    for n in range(len(fnames)):
        res = np.loadtxt(fnames[n])
        allresults.append(res)
    minlen = min([len(x) for x in allresults]);
    allrestrunc = np.array([x[:minlen] for x in allresults])
    allrestrunc = allrestrunc[:,::10]
    #allrestrunc = allrestrunc[:,:100]
    if numstr < len(strs)/4:
        mylinestyle='-'
        mymarker = 'o'
    elif numstr < len(strs)/2:
        mylinestyle='-'
        mymarker='+'
    elif numstr < 3*len(strs)/4:
        mylinestyle='--'
        mymarker='o'
    else:
        mylinestyle='--'
        mymarker='+'
    #plt.errorbar(range(allrestrunc.shape[1]), np.mean(allrestrunc, axis=0), yerr=np.std(allrestrunc, axis=0), label=strs[numstr], ls=mylinestyle, marker=mymarker)
    plt.plot(range(allrestrunc.shape[1]), np.mean(allrestrunc, axis=0), label=strs[numstr], ls=mylinestyle, marker=mymarker)
    print strs[numstr]+": maximum "+str(np.max(allrestrunc))

plt.legend(loc='best')
plt.show()



