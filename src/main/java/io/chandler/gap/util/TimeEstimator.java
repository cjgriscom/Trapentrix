package io.chandler.gap.util;

public class TimeEstimator {
	private long startTime;
	private long totalCombinations;

	public TimeEstimator(long totalCombinations) {
		this.startTime = System.currentTimeMillis();
		this.totalCombinations = totalCombinations;
	}


    public void checkProgressEstimate(int currentIteration, int results) {
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long estimatedTotalTime = (long) ((double) elapsedTime / currentIteration * totalCombinations);
        long remainingTime = estimatedTotalTime - elapsedTime;
        
        String remainingTimeStr = String.format("%d hours, %d minutes, %d seconds",
            remainingTime / 3600000,
            (remainingTime % 3600000) / 60000,
            (remainingTime % 60000) / 1000);
        
        System.out.println(currentIteration + " / " + totalCombinations + " -> " + results +
            " | Estimated time remaining: " + remainingTimeStr);

    }
}
