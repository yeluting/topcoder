package com.buaa.act.sdp.model.challenge;

/**
 * Created by YLT on 2016/10/17.
 */
public class ChallengeItem {

    private int challengeId;
    private String challengeName;
    private String typeName;
    private String challengeType;
    private int projectId;
    private int forumId;
    private String requirements;
    private String detailedRequirements;
    private int screeningScorecardId;
    private int reviewScorecardId;
    private int numberOfCheckpointsPrizes;
    private String topCheckPointPrize;
    private String currentStatus;
    private String postingDate;
    private String registrationEndDate;
    private String submissionEndDate;
    private String finalFixEndDate;
    private String appealsEndDate;
    private String checkpointSubmissionEndDate;
    private String forumLink;
    private String registrationStartDate;
    private double digitalRunPoints;
    private double reliabilityBonus;
    private String[] technology;
    private String[] languages;
    private String[] prize;
    private String[] platforms;
    private int numSubmissions;
    private int numRegistrants;
    private int duration;
    private double difficultyDegree;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getForumId() {
        return forumId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
    }

    public String getDetailedRequirements() {
        return detailedRequirements;
    }

    public void setDetailedRequirements(String detailedRequirements) {
        this.detailedRequirements = detailedRequirements;
    }

    public int getScreeningScorecardId() {
        return screeningScorecardId;
    }

    public void setScreeningScorecardId(int screeningScorecardId) {
        this.screeningScorecardId = screeningScorecardId;
    }

    public int getReviewScorecardId() {
        return reviewScorecardId;
    }

    public void setReviewScorecardId(int reviewScorecardId) {
        this.reviewScorecardId = reviewScorecardId;
    }

    public int getNumberOfCheckpointsPrizes() {
        return numberOfCheckpointsPrizes;
    }

    public void setNumberOfCheckpointsPrizes(int numberOfCheckpointsPrizes) {
        this.numberOfCheckpointsPrizes = numberOfCheckpointsPrizes;
    }

    public String getTopCheckPointPrize() {
        return topCheckPointPrize;
    }

    public void setTopCheckPointPrize(String topCheckPointPrize) {
        this.topCheckPointPrize = topCheckPointPrize;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public String getSubmissionEndDate() {
        return submissionEndDate;
    }

    public void setSubmissionEndDate(String submissionEndDate) {
        this.submissionEndDate = submissionEndDate;
    }

    public String getFinalFixEndDate() {
        return finalFixEndDate;
    }

    public void setFinalFixEndDate(String finalFixEndDate) {
        this.finalFixEndDate = finalFixEndDate;
    }

    public String getAppealsEndDate() {
        return appealsEndDate;
    }

    public void setAppealsEndDate(String appealsEndDate) {
        this.appealsEndDate = appealsEndDate;
    }

    public String getCheckpointSubmissionEndDate() {
        return checkpointSubmissionEndDate;
    }

    public void setCheckpointSubmissionEndDate(String checkpointSubmissionEndDate) {
        this.checkpointSubmissionEndDate = checkpointSubmissionEndDate;
    }

    public String getForumLink() {
        return forumLink;
    }

    public void setForumLink(String forumLink) {
        this.forumLink = forumLink;
    }

    public String getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(String registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public double getDigitalRunPoints() {
        return digitalRunPoints;
    }

    public void setDigitalRunPoints(double digitalRunPoints) {
        this.digitalRunPoints = digitalRunPoints;
    }

    public double getReliabilityBonus() {
        return reliabilityBonus;
    }

    public void setReliabilityBonus(double reliabilityBonus) {
        this.reliabilityBonus = reliabilityBonus;
    }

    public String[] getTechnology() {
        return technology;
    }

    public void setTechnology(String[] technology) {
        this.technology = technology;
    }

    public String[] getPrize() {
        return prize;
    }

    public void setPrize(String[] prize) {
        this.prize = prize;
    }

    public String[] getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String[] platforms) {
        this.platforms = platforms;
    }

    public int getNumSubmissions() {
        return numSubmissions;
    }

    public void setNumSubmissions(int numSubmissions) {
        this.numSubmissions = numSubmissions;
    }

    public int getNumRegistrants() {
        return numRegistrants;
    }

    public void setNumRegistrants(int numRegistrants) {
        this.numRegistrants = numRegistrants;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    @Override
    public String toString() {
        return challengeName + "+++" + detailedRequirements;
    }

    public double getDifficultyDegree() {
        return difficultyDegree;
    }

    public void setDifficultyDegree(double difficultyDegree) {
        this.difficultyDegree = difficultyDegree;
    }
}
