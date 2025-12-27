package com.example.demo.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * 고정 주차(월요일~일요일) 기반 날짜 계산 유틸리티
 */
public class DateUtils {

    /**
     * 이번 주의 시작 (월요일 00:00:00)
     */
    public static LocalDateTime getStartOfThisWeek() {
        return LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay();
    }

    /**
     * 이번 주의 끝 (일요일 23:59:59)
     */
    public static LocalDateTime getEndOfThisWeek() {
        return LocalDate.now()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(23, 59, 59);
    }

    /**
     * 지정된 날짜가 속한 주의 시작 (월요일 00:00:00)
     */
    public static LocalDateTime getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay();
    }

    /**
     * 지정된 날짜가 속한 주의 끝 (일요일 23:59:59)
     */
    public static LocalDateTime getEndOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(23, 59, 59);
    }

    /**
     * 이번 달의 시작 (1일 00:00:00)
     */
    public static LocalDateTime getStartOfThisMonth() {
        return LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay();
    }

    /**
     * 이번 달의 끝 (말일 23:59:59)
     */
    public static LocalDateTime getEndOfThisMonth() {
        return LocalDate.now()
            .with(TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59);
    }

    /**
     * 지정된 날짜가 속한 달의 시작 (1일 00:00:00)
     */
    public static LocalDateTime getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1)
            .atStartOfDay();
    }

    /**
     * 지정된 날짜가 속한 달의 끝 (말일 23:59:59)
     */
    public static LocalDateTime getEndOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth())
            .atTime(23, 59, 59);
    }

    /**
     * N주 전의 시작 (월요일 00:00:00)
     */
    public static LocalDateTime getStartOfWeekAgo(int weeksAgo) {
        LocalDate targetDate = LocalDate.now().minusWeeks(weeksAgo);
        return getStartOfWeek(targetDate);
    }

    /**
     * N주 전의 끝 (일요일 23:59:59)
     */
    public static LocalDateTime getEndOfWeekAgo(int weeksAgo) {
        LocalDate targetDate = LocalDate.now().minusWeeks(weeksAgo);
        return getEndOfWeek(targetDate);
    }

    /**
     * N개월 전의 시작 (1일 00:00:00)
     */
    public static LocalDateTime getStartOfMonthAgo(int monthsAgo) {
        LocalDate targetDate = LocalDate.now().minusMonths(monthsAgo);
        return getStartOfMonth(targetDate);
    }

    /**
     * N개월 전의 끝 (말일 23:59:59)
     */
    public static LocalDateTime getEndOfMonthAgo(int monthsAgo) {
        LocalDate targetDate = LocalDate.now().minusMonths(monthsAgo);
        return getEndOfMonth(targetDate);
    }

    /**
     * 주차 번호 계산 (ISO-8601 기준, 월요일이 1)
     */
    public static int getWeekOfYear(LocalDate date) {
        // ISO-8601 기준 주차 계산 (월요일이 1)
        return date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /**
     * 이번 주의 주차 번호
     */
    public static int getCurrentWeekOfYear() {
        return getWeekOfYear(LocalDate.now());
    }

    /**
     * 주차 표시 문자열 생성 (예: "2024년 1주차")
     */
    public static String getWeekDisplayString(LocalDate date) {
        int year = date.getYear();
        int week = getWeekOfYear(date);
        return String.format("%d년 %d주차", year, week);
    }

    /**
     * 월 표시 문자열 생성 (예: "2024년 1월")
     */
    public static String getMonthDisplayString(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        return String.format("%d년 %d월", year, month);
    }

    /**
     * 날짜 범위 표시 문자열 생성 (예: "1월 15일 ~ 1월 21일")
     */
    public static String getDateRangeDisplayString(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        if (startDate.getMonthValue() == endDate.getMonthValue()) {
            // 같은 달인 경우
            return String.format("%d월 %d일 ~ %d일", 
                startDate.getMonthValue(), startDate.getDayOfMonth(), endDate.getDayOfMonth());
        } else {
            // 다른 달인 경우
            return String.format("%d월 %d일 ~ %d월 %d일", 
                startDate.getMonthValue(), startDate.getDayOfMonth(),
                endDate.getMonthValue(), endDate.getDayOfMonth());
        }
    }

    /**
     * 주간 날짜 범위 표시 문자열 생성 (예: "월요일 ~ 일요일")
     */
    public static String getWeekRangeDisplayString() {
        return "월요일 ~ 일요일";
    }

    /**
     * 월간 날짜 범위 표시 문자열 생성 (예: "1일 ~ 31일")
     */
    public static String getMonthRangeDisplayString(LocalDate date) {
        int lastDay = date.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        return String.format("1일 ~ %d일", lastDay);
    }

    /**
     * 시간을 분 단위로 변환
     */
    public static int convertSecondsToMinutes(int seconds) {
        return seconds / 60;
    }

    /**
     * 시간을 시간:분 형식으로 변환
     */
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d시간 %d분", hours, minutes);
        } else {
            return String.format("%d분", minutes);
        }
    }

    /**
     * 시간을 간단한 형식으로 변환 (예: "1h 30m", "45m")
     */
    public static String formatTimeSimple(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
