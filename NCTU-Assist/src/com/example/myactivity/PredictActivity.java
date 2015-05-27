package com.example.myactivity;

public class PredictActivity {
	String activity;
	double probability;
	public PredictActivity(String act, String prob) {
		activity = act;
		probability = Double.valueOf(prob);
	}
	public PredictActivity() {
		activity = null;
		probability = 0;
	}
	public PredictActivity(String act, double prob) {
		activity = act;
		probability = prob;
	}
	public String getActivity() {
		return activity;
	}
	public void setActicity(String act) {
		activity = act;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double prob) {
		probability = prob;
	}
	public void setProbability(String prob) {
		probability = Double.valueOf(prob);
	}
}
