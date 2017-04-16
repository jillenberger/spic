/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.source.mid2008.generator;

/**
 * @author johannes
 */
public interface VariableNames {

    String HOUSEHOLD_ID = "hhid";

    String PERSON_ID = "pid";

    String PERSON_AGE = "hp_alter";

    String PERSON_CARAVAIL = "p01_1";

    String SURVEY_DAY = "stichtag";

    String SURVEY_MONTH = "stich_m";

    String HH_INCOME = "hheink";

    String HH_MEMEBERS = "h02";

    String PERSON_LAU2_CLASS = "polgk";

    String PERSON_SEX = "hp_sex";

    String PERSON_STATE = "bland";

    String PERSON_WEIGHT = "p_gew";

    String LEG_DISTANCE = "wegkm_k";

    String LEG_MODE = "hvm";
    String LEG_MAIN_TYPE = "w04";
    String LEG_SUB_TYPE = "w04_dzw";
    String LEG_ORIGIN = "w01";
    String LEG_DESTINATION = "w13";
    String LEG_INDEX = "wsid";
    String LEG_START_TIME_HOUR = "st_std";
    String LEG_START_TIME_MIN = "st_min";
    String LEG_END_TIME_HOUR = "en_std";
    String LEG_END_TIME_MIN = "en_min";
    String START_NEXT_DAY = "st_dat";
    String END_NEXT_DAY = "en_dat";

    String JOURNEY_DISTANCE = "p1016";

    String JOURNEY_MODE = "hvm_r";

    String JOURNEY_PURPOSE = "p101";

    String JOURNEY_NIGHTS = "p1014";

    String JOURNEY_DESTINATION = "p1012";
}
