package com.tiger.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class LeetcodeProfile {
	private String name ;
	private String avatar ;
	private String about ;
	private String school ;
    private int totalSolved;
    private List<TotalSubmission> totalSubmissions;
    private int totalQuestions;
    private int easySolved;
    private int totalEasy;
    private int mediumSolved;
    private int totalMedium;
    private int hardSolved;
    private int totalHard;
    private int ranking;
    private int contributionPoint;
    private int reputation;
    private Map<String, Integer> submissionCalendar;
    private List<RecentSubmission> recentSubmissions;
    private MatchedUserStats matchedUserStats;
    @Data
    public static class TotalSubmission {
        private String difficulty;
        private int count;
        private int submissions;

    }
    @Data
    public static class RecentSubmission {
        private String title;
        private String titleSlug;
        private String timestamp;
        private String statusDisplay;
        private String lang;
        private String __typename;

    }
    @Data
    public static class MatchedUserStats {
        private List<AcSubmissionNum> acSubmissionNum;
        private List<TotalSubmissionNum> totalSubmissionNum;
        @Data
        public static class AcSubmissionNum {
            private String difficulty;
            private int count;
            private int submissions;
        }
        @Data
        public static class TotalSubmissionNum {
            private String difficulty;
            private int count;
            private int submissions;
        }
    }
}
