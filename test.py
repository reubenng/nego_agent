import pandas as pd
import numpy as np
np.set_printoptions(threshold='nan')
df = pd.read_csv('./tournament-20171212-145224-party_domain1.log.csv')
# print(df['Profile 1'].tail)
del df['Exception']
df = df.dropna(axis=0, how='any')

cont_1 = []
cont_2 = []
cont_3 = []

n_profiles = 9

profiles = [1, 2, 3, 4, 5, 6, 7, 8, 9]

combinations = []

for p_1 in range(n_profiles):
    for p_2 in range(n_profiles):
        for p_3 in range(n_profiles):
            content = [p_1 + 1, p_2 + 1, p_3 + 1]
            content = np.array(content)
            content = np.unique(content)
            content_size = content.size
            if int(content_size) < 3:
                continue
            else:
                combinations.append([p_1 + 1, p_2 + 1, p_3 + 1])

combinations = np.array(combinations)

for combination in range(combinations.shape[0]):
    combinations[combination] = np.sort(combinations[combination])



unique_combinations = np.unique(combinations, axis=0)

row = []
for i in range(unique_combinations.shape[0]):
    columns = []
    for j in range(unique_combinations.shape[1]):
        columns.append("party" + str(unique_combinations[i][j]) + "_utility.xml")

    row.append(columns)
    columns = []

row = np.array(row)

unique_combinations = row

simulation_profiles = df[['Profile 1', 'Profile 2', 'Profile 3']]

simulation_profiles = np.array(simulation_profiles)


mask = []

for i, comb in enumerate(unique_combinations):
    for j, sim in enumerate(simulation_profiles):
        unique_comb = np.unique(comb)
        unique_sim = np.unique(sim)

        if (unique_comb==unique_sim).all() and (float(df['Utility 1'][j]) == 0):
            #
            # if df['Dist. to Nash'][j]:
            mask.append(j)
            break
            # else:
            #     continue

mask = np.array(mask)

nash_distances = df['Dist. to Nash'][mask]

nash_distances = np.array(nash_distances)

df = pd.read_csv('./tournament-20171212-123533-party_domain.log.csv')

rows = df.shape[0]

nash_average = []

for row in range(rows):
    if "Agent33" in str(df["Agent 1"][row]) or "Agent33" in str(df["Agent 2"][row]) or "Agent33" in str(df["Agent 3"][row]):
        profiles = [df["Profile 1"][row], df["Profile 2"][row], df["Profile 3"][row]]
        agent_n = 0
        if "Agent33" in str(df["Agent 1"][row]):
            agent_n = 1
        elif "Agent33" in str(df["Agent 2"][row]):
            agent_n = 2
        elif "Agent33" in str(df["Agent 3"][row]):
            agent_n = 3

        for combination in range(unique_combinations.shape[0]):
            profile = np.unique(profiles)
            unique_comb = np.unique(unique_combinations[combination])
            if (unique_comb==profile).all():
                nash_max = float(nash_distances[combination])
                nash_value = float(df['Dist. to Nash'][row])
                norm_nash = 1 - nash_value / nash_max

                if agent_n == 1:
                    utility = df['Utility 1'][row]
                elif agent_n == 2:
                    utility = df['Utility 2'][row]
                elif agent_n == 3:
                    utility = df['Utility 3'][row]

                norm_score = (norm_nash + utility) / 2

                nash_average.append(norm_score)

nash_average = np.array(nash_average)

average_score = nash_average.sum() / nash_average.size

print("Average score: " + str(average_score))
