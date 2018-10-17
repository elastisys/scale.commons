package com.elastisys.scale.commons.util.countries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Enumeration for the countries of the world.
 * <p/>
 * NOTE: since countries may change with every Java release the enumeration
 * constants may need to be updated. To generate a new list of country
 * enumeration constants, simply run the {@link #main(String[])} method of this
 * class.
 */
public enum Countries {
    /** The country of Andorra. */
    ANDORRA("Andorra"),
    /** The country of United Arab Emirates. */
    UNITED_ARAB_EMIRATES("United Arab Emirates"),
    /** The country of Afghanistan. */
    AFGHANISTAN("Afghanistan"),
    /** The country of Antigua & Barbuda. */
    ANTIGUA_AND_BARBUDA("Antigua & Barbuda"),
    /** The country of Anguilla. */
    ANGUILLA("Anguilla"),
    /** The country of Albania. */
    ALBANIA("Albania"),
    /** The country of Armenia. */
    ARMENIA("Armenia"),
    /** The country of Angola. */
    ANGOLA("Angola"),
    /** The country of Antarctica. */
    ANTARCTICA("Antarctica"),
    /** The country of Argentina. */
    ARGENTINA("Argentina"),
    /** The country of American Samoa. */
    AMERICAN_SAMOA("American Samoa"),
    /** The country of Austria. */
    AUSTRIA("Austria"),
    /** The country of Australia. */
    AUSTRALIA("Australia"),
    /** The country of Aruba. */
    ARUBA("Aruba"),
    /** The country of Åland Islands. */
    ÅLAND_ISLANDS("Åland Islands"),
    /** The country of Azerbaijan. */
    AZERBAIJAN("Azerbaijan"),
    /** The country of Bosnia & Herzegovina. */
    BOSNIA_AND_HERZEGOVINA("Bosnia & Herzegovina"),
    /** The country of Barbados. */
    BARBADOS("Barbados"),
    /** The country of Bangladesh. */
    BANGLADESH("Bangladesh"),
    /** The country of Belgium. */
    BELGIUM("Belgium"),
    /** The country of Burkina Faso. */
    BURKINA_FASO("Burkina Faso"),
    /** The country of Bulgaria. */
    BULGARIA("Bulgaria"),
    /** The country of Bahrain. */
    BAHRAIN("Bahrain"),
    /** The country of Burundi. */
    BURUNDI("Burundi"),
    /** The country of Benin. */
    BENIN("Benin"),
    /** The country of St. Barthélemy. */
    ST_BARTHELEMY("St. Barthélemy"),
    /** The country of Bermuda. */
    BERMUDA("Bermuda"),
    /** The country of Brunei. */
    BRUNEI("Brunei"),
    /** The country of Bolivia. */
    BOLIVIA("Bolivia"),
    /** The country of Caribbean Netherlands. */
    CARIBBEAN_NETHERLANDS("Caribbean Netherlands"),
    /** The country of Brazil. */
    BRAZIL("Brazil"),
    /** The country of Bahamas. */
    BAHAMAS("Bahamas"),
    /** The country of Bhutan. */
    BHUTAN("Bhutan"),
    /** The country of Bouvet Island. */
    BOUVET_ISLAND("Bouvet Island"),
    /** The country of Botswana. */
    BOTSWANA("Botswana"),
    /** The country of Belarus. */
    BELARUS("Belarus"),
    /** The country of Belize. */
    BELIZE("Belize"),
    /** The country of Canada. */
    CANADA("Canada"),
    /** The country of Cocos (Keeling) Islands. */
    COCOS_KEELING_ISLANDS("Cocos (Keeling) Islands"),
    /** The country of Congo - Kinshasa. */
    CONGO_KINSHASA("Congo - Kinshasa"),
    /** The country of Central African Republic. */
    CENTRAL_AFRICAN_REPUBLIC("Central African Republic"),
    /** The country of Congo - Brazzaville. */
    CONGO_BRAZZAVILLE("Congo - Brazzaville"),
    /** The country of Switzerland. */
    SWITZERLAND("Switzerland"),
    /** The country of Côte d’Ivoire. */
    COTE_DIVOIRE("Côte d’Ivoire"),
    /** The country of Cook Islands. */
    COOK_ISLANDS("Cook Islands"),
    /** The country of Chile. */
    CHILE("Chile"),
    /** The country of Cameroon. */
    CAMEROON("Cameroon"),
    /** The country of China. */
    CHINA("China"),
    /** The country of Colombia. */
    COLOMBIA("Colombia"),
    /** The country of Costa Rica. */
    COSTA_RICA("Costa Rica"),
    /** The country of Cuba. */
    CUBA("Cuba"),
    /** The country of Cape Verde. */
    CAPE_VERDE("Cape Verde"),
    /** The country of Curaçao. */
    CURAÇAO("Curaçao"),
    /** The country of Christmas Island. */
    CHRISTMAS_ISLAND("Christmas Island"),
    /** The country of Cyprus. */
    CYPRUS("Cyprus"),
    /** The country of Czech Republic. */
    CZECH_REPUBLIC("Czech Republic"),
    /** The country of Germany. */
    GERMANY("Germany"),
    /** The country of Djibouti. */
    DJIBOUTI("Djibouti"),
    /** The country of Denmark. */
    DENMARK("Denmark"),
    /** The country of Dominica. */
    DOMINICA("Dominica"),
    /** The country of Dominican Republic. */
    DOMINICAN_REPUBLIC("Dominican Republic"),
    /** The country of Algeria. */
    ALGERIA("Algeria"),
    /** The country of Ecuador. */
    ECUADOR("Ecuador"),
    /** The country of Estonia. */
    ESTONIA("Estonia"),
    /** The country of Egypt. */
    EGYPT("Egypt"),
    /** The country of Western Sahara. */
    WESTERN_SAHARA("Western Sahara"),
    /** The country of Eritrea. */
    ERITREA("Eritrea"),
    /** The country of Spain. */
    SPAIN("Spain"),
    /** The country of Ethiopia. */
    ETHIOPIA("Ethiopia"),
    /** The country of Finland. */
    FINLAND("Finland"),
    /** The country of Fiji. */
    FIJI("Fiji"),
    /** The country of Falkland Islands. */
    FALKLAND_ISLANDS("Falkland Islands"),
    /** The country of Micronesia. */
    MICRONESIA("Micronesia"),
    /** The country of Faroe Islands. */
    FAROE_ISLANDS("Faroe Islands"),
    /** The country of France. */
    FRANCE("France"),
    /** The country of Gabon. */
    GABON("Gabon"),
    /** The country of United Kingdom. */
    UNITED_KINGDOM("United Kingdom"),
    /** The country of Grenada. */
    GRENADA("Grenada"),
    /** The country of Georgia. */
    GEORGIA("Georgia"),
    /** The country of French Guiana. */
    FRENCH_GUIANA("French Guiana"),
    /** The country of Guernsey. */
    GUERNSEY("Guernsey"),
    /** The country of Ghana. */
    GHANA("Ghana"),
    /** The country of Gibraltar. */
    GIBRALTAR("Gibraltar"),
    /** The country of Greenland. */
    GREENLAND("Greenland"),
    /** The country of Gambia. */
    GAMBIA("Gambia"),
    /** The country of Guinea. */
    GUINEA("Guinea"),
    /** The country of Guadeloupe. */
    GUADELOUPE("Guadeloupe"),
    /** The country of Equatorial Guinea. */
    EQUATORIAL_GUINEA("Equatorial Guinea"),
    /** The country of Greece. */
    GREECE("Greece"),
    /** The country of South Georgia & South Sandwich Islands. */
    SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS("South Georgia & South Sandwich Islands"),
    /** The country of Guatemala. */
    GUATEMALA("Guatemala"),
    /** The country of Guam. */
    GUAM("Guam"),
    /** The country of Guinea-Bissau. */
    GUINEA_BISSAU("Guinea-Bissau"),
    /** The country of Guyana. */
    GUYANA("Guyana"),
    /** The country of Hong Kong SAR China. */
    HONG_KONG_SAR_CHINA("Hong Kong SAR China"),
    /** The country of Heard & McDonald Islands. */
    HEARD_AND_MCDONALD_ISLANDS("Heard & McDonald Islands"),
    /** The country of Honduras. */
    HONDURAS("Honduras"),
    /** The country of Croatia. */
    CROATIA("Croatia"),
    /** The country of Haiti. */
    HAITI("Haiti"),
    /** The country of Hungary. */
    HUNGARY("Hungary"),
    /** The country of Indonesia. */
    INDONESIA("Indonesia"),
    /** The country of Ireland. */
    IRELAND("Ireland"),
    /** The country of Israel. */
    ISRAEL("Israel"),
    /** The country of Isle of Man. */
    ISLE_OF_MAN("Isle of Man"),
    /** The country of India. */
    INDIA("India"),
    /** The country of British Indian Ocean Territory. */
    BRITISH_INDIAN_OCEAN_TERRITORY("British Indian Ocean Territory"),
    /** The country of Iraq. */
    IRAQ("Iraq"),
    /** The country of Iran. */
    IRAN("Iran"),
    /** The country of Iceland. */
    ICELAND("Iceland"),
    /** The country of Italy. */
    ITALY("Italy"),
    /** The country of Jersey. */
    JERSEY("Jersey"),
    /** The country of Jamaica. */
    JAMAICA("Jamaica"),
    /** The country of Jordan. */
    JORDAN("Jordan"),
    /** The country of Japan. */
    JAPAN("Japan"),
    /** The country of Kenya. */
    KENYA("Kenya"),
    /** The country of Kyrgyzstan. */
    KYRGYZSTAN("Kyrgyzstan"),
    /** The country of Cambodia. */
    CAMBODIA("Cambodia"),
    /** The country of Kiribati. */
    KIRIBATI("Kiribati"),
    /** The country of Comoros. */
    COMOROS("Comoros"),
    /** The country of St. Kitts & Nevis. */
    ST_KITTS_AND_NEVIS("St. Kitts & Nevis"),
    /** The country of North Korea. */
    NORTH_KOREA("North Korea"),
    /** The country of South Korea. */
    SOUTH_KOREA("South Korea"),
    /** The country of Kuwait. */
    KUWAIT("Kuwait"),
    /** The country of Cayman Islands. */
    CAYMAN_ISLANDS("Cayman Islands"),
    /** The country of Kazakhstan. */
    KAZAKHSTAN("Kazakhstan"),
    /** The country of Laos. */
    LAOS("Laos"),
    /** The country of Lebanon. */
    LEBANON("Lebanon"),
    /** The country of St. Lucia. */
    ST_LUCIA("St. Lucia"),
    /** The country of Liechtenstein. */
    LIECHTENSTEIN("Liechtenstein"),
    /** The country of Sri Lanka. */
    SRI_LANKA("Sri Lanka"),
    /** The country of Liberia. */
    LIBERIA("Liberia"),
    /** The country of Lesotho. */
    LESOTHO("Lesotho"),
    /** The country of Lithuania. */
    LITHUANIA("Lithuania"),
    /** The country of Luxembourg. */
    LUXEMBOURG("Luxembourg"),
    /** The country of Latvia. */
    LATVIA("Latvia"),
    /** The country of Libya. */
    LIBYA("Libya"),
    /** The country of Morocco. */
    MOROCCO("Morocco"),
    /** The country of Monaco. */
    MONACO("Monaco"),
    /** The country of Moldova. */
    MOLDOVA("Moldova"),
    /** The country of Montenegro. */
    MONTENEGRO("Montenegro"),
    /** The country of St. Martin. */
    ST_MARTIN("St. Martin"),
    /** The country of Madagascar. */
    MADAGASCAR("Madagascar"),
    /** The country of Marshall Islands. */
    MARSHALL_ISLANDS("Marshall Islands"),
    /** The country of Macedonia. */
    MACEDONIA("Macedonia"),
    /** The country of Mali. */
    MALI("Mali"),
    /** The country of Myanmar (Burma). */
    MYANMAR_BURMA("Myanmar (Burma)"),
    /** The country of Mongolia. */
    MONGOLIA("Mongolia"),
    /** The country of Macau SAR China. */
    MACAU_SAR_CHINA("Macau SAR China"),
    /** The country of Northern Mariana Islands. */
    NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands"),
    /** The country of Martinique. */
    MARTINIQUE("Martinique"),
    /** The country of Mauritania. */
    MAURITANIA("Mauritania"),
    /** The country of Montserrat. */
    MONTSERRAT("Montserrat"),
    /** The country of Malta. */
    MALTA("Malta"),
    /** The country of Mauritius. */
    MAURITIUS("Mauritius"),
    /** The country of Maldives. */
    MALDIVES("Maldives"),
    /** The country of Malawi. */
    MALAWI("Malawi"),
    /** The country of Mexico. */
    MEXICO("Mexico"),
    /** The country of Malaysia. */
    MALAYSIA("Malaysia"),
    /** The country of Mozambique. */
    MOZAMBIQUE("Mozambique"),
    /** The country of Namibia. */
    NAMIBIA("Namibia"),
    /** The country of New Caledonia. */
    NEW_CALEDONIA("New Caledonia"),
    /** The country of Niger. */
    NIGER("Niger"),
    /** The country of Norfolk Island. */
    NORFOLK_ISLAND("Norfolk Island"),
    /** The country of Nigeria. */
    NIGERIA("Nigeria"),
    /** The country of Nicaragua. */
    NICARAGUA("Nicaragua"),
    /** The country of Netherlands. */
    NETHERLANDS("Netherlands"),
    /** The country of Norway. */
    NORWAY("Norway"),
    /** The country of Nepal. */
    NEPAL("Nepal"),
    /** The country of Nauru. */
    NAURU("Nauru"),
    /** The country of Niue. */
    NIUE("Niue"),
    /** The country of New Zealand. */
    NEW_ZEALAND("New Zealand"),
    /** The country of Oman. */
    OMAN("Oman"),
    /** The country of Panama. */
    PANAMA("Panama"),
    /** The country of Peru. */
    PERU("Peru"),
    /** The country of French Polynesia. */
    FRENCH_POLYNESIA("French Polynesia"),
    /** The country of Papua New Guinea. */
    PAPUA_NEW_GUINEA("Papua New Guinea"),
    /** The country of Philippines. */
    PHILIPPINES("Philippines"),
    /** The country of Pakistan. */
    PAKISTAN("Pakistan"),
    /** The country of Poland. */
    POLAND("Poland"),
    /** The country of St. Pierre & Miquelon. */
    ST_PIERRE_AND_MIQUELON("St. Pierre & Miquelon"),
    /** The country of Pitcairn Islands. */
    PITCAIRN_ISLANDS("Pitcairn Islands"),
    /** The country of Puerto Rico. */
    PUERTO_RICO("Puerto Rico"),
    /** The country of Palestinian Territories. */
    PALESTINIAN_TERRITORIES("Palestinian Territories"),
    /** The country of Portugal. */
    PORTUGAL("Portugal"),
    /** The country of Palau. */
    PALAU("Palau"),
    /** The country of Paraguay. */
    PARAGUAY("Paraguay"),
    /** The country of Qatar. */
    QATAR("Qatar"),
    /** The country of Réunion. */
    RÉUNION("Réunion"),
    /** The country of Romania. */
    ROMANIA("Romania"),
    /** The country of Serbia. */
    SERBIA("Serbia"),
    /** The country of Russia. */
    RUSSIA("Russia"),
    /** The country of Rwanda. */
    RWANDA("Rwanda"),
    /** The country of Saudi Arabia. */
    SAUDI_ARABIA("Saudi Arabia"),
    /** The country of Solomon Islands. */
    SOLOMON_ISLANDS("Solomon Islands"),
    /** The country of Seychelles. */
    SEYCHELLES("Seychelles"),
    /** The country of Sudan. */
    SUDAN("Sudan"),
    /** The country of Sweden. */
    SWEDEN("Sweden"),
    /** The country of Singapore. */
    SINGAPORE("Singapore"),
    /** The country of St. Helena. */
    ST_HELENA("St. Helena"),
    /** The country of Slovenia. */
    SLOVENIA("Slovenia"),
    /** The country of Svalbard & Jan Mayen. */
    SVALBARD_AND_JAN_MAYEN("Svalbard & Jan Mayen"),
    /** The country of Slovakia. */
    SLOVAKIA("Slovakia"),
    /** The country of Sierra Leone. */
    SIERRA_LEONE("Sierra Leone"),
    /** The country of San Marino. */
    SAN_MARINO("San Marino"),
    /** The country of Senegal. */
    SENEGAL("Senegal"),
    /** The country of Somalia. */
    SOMALIA("Somalia"),
    /** The country of Suriname. */
    SURINAME("Suriname"),
    /** The country of South Sudan. */
    SOUTH_SUDAN("South Sudan"),
    /** The country of São Tomé & Príncipe. */
    SÃO_TOMÉ_AND_PRÍNCIPE("São Tomé & Príncipe"),
    /** The country of El Salvador. */
    EL_SALVADOR("El Salvador"),
    /** The country of Sint Maarten. */
    SINT_MAARTEN("Sint Maarten"),
    /** The country of Syria. */
    SYRIA("Syria"),
    /** The country of Swaziland. */
    SWAZILAND("Swaziland"),
    /** The country of Turks & Caicos Islands. */
    TURKS_AND_CAICOS_ISLANDS("Turks & Caicos Islands"),
    /** The country of Chad. */
    CHAD("Chad"),
    /** The country of French Southern Territories. */
    FRENCH_SOUTHERN_TERRITORIES("French Southern Territories"),
    /** The country of Togo. */
    TOGO("Togo"),
    /** The country of Thailand. */
    THAILAND("Thailand"),
    /** The country of Tajikistan. */
    TAJIKISTAN("Tajikistan"),
    /** The country of Tokelau. */
    TOKELAU("Tokelau"),
    /** The country of Timor-Leste. */
    TIMOR_LESTE("Timor-Leste"),
    /** The country of Turkmenistan. */
    TURKMENISTAN("Turkmenistan"),
    /** The country of Tunisia. */
    TUNISIA("Tunisia"),
    /** The country of Tonga. */
    TONGA("Tonga"),
    /** The country of Turkey. */
    TURKEY("Turkey"),
    /** The country of Trinidad & Tobago. */
    TRINIDAD_AND_TOBAGO("Trinidad & Tobago"),
    /** The country of Tuvalu. */
    TUVALU("Tuvalu"),
    /** The country of Taiwan. */
    TAIWAN("Taiwan"),
    /** The country of Tanzania. */
    TANZANIA("Tanzania"),
    /** The country of Ukraine. */
    UKRAINE("Ukraine"),
    /** The country of Uganda. */
    UGANDA("Uganda"),
    /** The country of U.S. Outlying Islands. */
    US_OUTLYING_ISLANDS("U.S. Outlying Islands"),
    /** The country of United States. */
    UNITED_STATES("United States"),
    /** The country of Uruguay. */
    URUGUAY("Uruguay"),
    /** The country of Uzbekistan. */
    UZBEKISTAN("Uzbekistan"),
    /** The country of Vatican City. */
    VATICAN_CITY("Vatican City"),
    /** The country of St. Vincent & Grenadines. */
    ST_VINCENT_AND_GRENADINES("St. Vincent & Grenadines"),
    /** The country of Venezuela. */
    VENEZUELA("Venezuela"),
    /** The country of British Virgin Islands. */
    BRITISH_VIRGIN_ISLANDS("British Virgin Islands"),
    /** The country of U.S. Virgin Islands. */
    US_VIRGIN_ISLANDS("U.S. Virgin Islands"),
    /** The country of Vietnam. */
    VIETNAM("Vietnam"),
    /** The country of Vanuatu. */
    VANUATU("Vanuatu"),
    /** The country of Wallis & Futuna. */
    WALLIS_AND_FUTUNA("Wallis & Futuna"),
    /** The country of Samoa. */
    SAMOA("Samoa"),
    /** The country of Yemen. */
    YEMEN("Yemen"),
    /** The country of Mayotte. */
    MAYOTTE("Mayotte"),
    /** The country of South Africa. */
    SOUTH_AFRICA("South Africa"),
    /** The country of Zambia. */
    ZAMBIA("Zambia"),
    /** The country of Zimbabwe. */
    ZIMBABWE("Zimbabwe");

    /**
     * Tracks all known countries keyed by country display name (in english and
     * lower case), such as 'united states', and mapped to a {@link Locale} for
     * that country.
     */
    private final static Map<String, Locale> COUNTRIES = new HashMap<>();

    static {
        // build the map of countries
        String[] countryCodes = Locale.getISOCountries();
        for (String countryCode : countryCodes) {
            Locale locale = new Locale.Builder().setRegion(countryCode).build();
            // note: use US locale to get the country name in English
            String countryName = locale.getDisplayCountry(Locale.US).toLowerCase();
            COUNTRIES.put(countryName, locale);
        }
    }

    /** The country name (such as "United States") for the country. */
    private final String countryName;

    Countries(String countryName) {
        this.countryName = countryName;
    }

    /**
     * Returns the country name (such as "United States") for the country.
     *
     * @return
     */
    public String getCountryName() {
        return this.countryName;
    }

    /**
     * Determines if the given string is the name of a country in the world. The
     * method recognizes countries for which there exists a Java {@link Locale}.
     * The comparison is case insensitive.
     *
     * @param countryDisplayName
     *            The display name of the country. Such as 'Sweden' or 'United
     *            States'.
     * @return <code>true</code> if the country exists, <code>false</code>
     *         otherwise.
     */
    public static boolean countryExists(String countryDisplayName) {
        return COUNTRIES.containsKey(countryDisplayName.toLowerCase());
    }

    /**
     * Main method, only included to allow simple re-generation of the
     * enumeration constants in this enum for a new Java release.
     *
     * @param args
     */
    public static void main(String[] args) {
        String[] isoCountries = Locale.getISOCountries();
        for (int i = 0; i < isoCountries.length; i++) {
            String countryCode = isoCountries[i];
            Locale locale = new Locale.Builder().setRegion(countryCode).build();
            // note: use US locale to get the country name in English
            String countryName = locale.getDisplayCountry(Locale.US).toLowerCase();
            countryName = locale.getDisplayCountry(Locale.US);
            String enumConstant = countryName.toUpperCase().replaceAll(" ", "_").replaceAll("&", "AND")
                    .replaceAll("\\.", "").replaceAll("\\-", "_").replaceAll("\\(", "").replaceAll("\\)", "")
                    .replaceAll("_+", "_");

            System.out.println("/** The country of " + countryName + ". */");
            System.out.print(enumConstant + "(\"" + countryName + "\")");
            if (i < isoCountries.length - 1) {
                System.out.println(",");
            } else {
                System.out.println(";");
            }
        }
    }
}
