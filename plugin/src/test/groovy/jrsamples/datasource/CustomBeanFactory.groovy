/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2016 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package jrsamples.datasource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
class CustomBeanFactory {

    private static CustomBean[] data =[
            new CustomBean("Berne", new Integer(9), "James Schneider", "277 Seventh Av.",35,"EUR"),
            new CustomBean("Berne", new Integer(22), "Bill Ott", "250 - 20th Ave.",25,"USD"),
            new CustomBean("Boston", new Integer(23), "Julia Heiniger", "358 College Av.",55,"USD"),
            new CustomBean("Boston", new Integer(32), "Michael Ott", "339 College Av.",175,"USD"),
            new CustomBean("Chicago", new Integer(39), "Mary Karsen", "202 College Av.",100,"USD"),
            new CustomBean("Chicago", new Integer(35), "George Karsen", "412 College Av.",100,"EUR"),
            new CustomBean("Chicago", new Integer(11), "Julia White", "412 Upland Pl.",100,"USD"),
            new CustomBean("Dallas", new Integer(47), "Janet Fuller", "445 Upland Pl.",100,"USD"),
            new CustomBean("Dallas", new Integer(43), "Susanne Smith", "2 Upland Pl.",50,"USD"),
            new CustomBean("Dallas", new Integer(40), "Susanne Miller", "440 - 20th Ave.",100,"EUR"),
            new CustomBean("Dallas", new Integer(36), "John Steel", "276 Upland Pl.",100,"USD"),
            new CustomBean("Dallas", new Integer(37), "Michael Clancy", "19 Seventh Av.",100,"USD"),
            new CustomBean("Dallas", new Integer(19), "Susanne Heiniger", "86 - 20th Ave.",50,"USD"),
            new CustomBean("Dallas", new Integer(10), "Anne Fuller", "135 Upland Pl.",100,"EUR"),
            new CustomBean("Dallas", new Integer(4), "Sylvia Ringer", "365 College Av.",100,"USD"),
            new CustomBean("Dallas", new Integer(0), "Laura Steel", "429 Seventh Av.",100,"USD"),
            new CustomBean("Lyon", new Integer(38), "Andrew Heiniger", "347 College Av.",50,"EUR"),
            new CustomBean("Lyon", new Integer(28), "Susanne White", "74 - 20th Ave.",100,"USD"),
            new CustomBean("Lyon", new Integer(17), "Laura Ott", "443 Seventh Av.",100,"USD"),
            new CustomBean("Lyon", new Integer(2), "Anne Miller", "20 Upland Pl.",50,"USD"),
            new CustomBean("New York", new Integer(46), "Andrew May", "172 Seventh Av.",100,"EUR"),
            new CustomBean("New York", new Integer(44), "Sylvia Ott", "361 College Av.",100,"USD"),
            new CustomBean("New York", new Integer(41), "Bill King", "546 College Av.",50,"USD"),
            new CustomBean("Oslo", new Integer(45), "Janet May", "396 Seventh Av.",100,"EUR"),
            new CustomBean("Oslo", new Integer(42), "Robert Ott", "503 Seventh Av.",100,"USD"),
            new CustomBean("Paris", new Integer(25), "Sylvia Steel", "269 College Av.",50,"USD"),
            new CustomBean("Paris", new Integer(18), "Sylvia Fuller", "158 - 20th Ave.",100,"EUR"),
            new CustomBean("Paris", new Integer(5), "Laura Miller", "294 Seventh Av.",100,"USD"),
            new CustomBean("San Francisco", new Integer(48), "Robert White", "549 Seventh Av.",100,"USD"),
            new CustomBean("San Francisco", new Integer(7), "James Peterson", "231 Upland Pl.",50,"EUR")
    ]


    /**
     *
     */
    public static Object[] getBeanArray()
    {
        return data;
    }


    /**
     *
     */
    public static Collection<CustomBean> getBeanCollection()
    {
        return Arrays.asList(data);
    }

    static List<Map> listMap = [
        [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
        [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]
}
