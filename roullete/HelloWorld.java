public class HelloWorld {
    public static void main(String a[]){

        // Arbitrary Profile variable
        double[][] profile = new double[][]{
            { 0.7, 0.2, 0.1},
            { 0.6, 0.3, 0.1},
            { 0.2, 0.3, 0.5},
            { 0.7, 0.2, 0.1},
            { 0.5, 0.3, 0.2}
        };

        // Arbitrary Profile mapping
        String[][] profile_map = new String[][]{
            { "Cola", "Sprite", "Fanta"},
            { "Hotdog", "Burger", "Pizza"},
            { "TV", "BoardGames", "Dancing"},
            { "MC", "BK", "Wendys"},
            { "1-night", "2-nights", "3-nights"}
        };

        // 1) get number of issues (rows) from the profile variable
        int NumberOfIssues = profile.length;
        int NumberOfValues = profile[0].length;

        String[] picked_values = new String[NumberOfIssues];
        int[] picked_values_index = new int[NumberOfIssues];

        // 2) create vector (size=NumberOfIssues) of random numbers from 0 to 1
        double[] randomArray = new double[NumberOfIssues];
        double offset = 0;
        
        for(int issue_n = 0; issue_n < NumberOfIssues; issue_n++) {
            randomArray[issue_n] = Math.random();
            System.out.println(randomArray[issue_n]);
        }

        // 3) apply roullete selection
        for(int issue = 0; issue < NumberOfIssues; issue++) {

            // clear offset
            offset = profile[issue][0];

            for(int value = 0; value < NumberOfValues; value++) {
                
                // if it falls in the pie section store the value from the profile map
                if (randomArray[issue] < offset) {
                    picked_values[issue] = profile_map[issue][value];
                    picked_values_index[issue] = value;
                    break;

                // else increment offset by the appropriate value
                } else {
                    offset = offset + profile[issue][value + 1];
                }
            }
        }

        // 3.1) Debug results
        for(int issue = 0; issue < NumberOfIssues; issue++) {
            // System.out.println("Picked value: " + picked_values[issue]);
            System.out.println("Picked index: " + picked_values_index[issue]);
        }
        
        // 4) Generate new bid
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
        
        HashMap<Integer, Value> issueMap = new HashMap<>();
        int issue_n = 0;
        for (Issue issue : issues) {
            // Since discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            List<ValueDiscrete> discreteValues = new ArrayList<>();
            discreteValues.addAll(issueDiscrete.getValues());
            // pick a random value
            // Collections.shuffle(discreteValues);
            issueMap.put(issue.getNumber(), picked_values_index[issue_n]);
            issue_n++;
        }
        
        Bid newBid = new Bid(info.getUtilitySpace().getDomain(), issueMap);

    }
}