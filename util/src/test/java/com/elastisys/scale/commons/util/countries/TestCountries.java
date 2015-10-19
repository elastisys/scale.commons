package com.elastisys.scale.commons.util.countries;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Exercises the {@link Countries} class.
 */
public class TestCountries {

	private final List<String> knownCountries = Arrays.asList("Andorra",
			"United Arab Emirates", "Afghanistan", "Antigua and Barbuda",
			"Anguilla", "Albania", "Armenia", "Netherlands Antilles", "Angola",
			"Antarctica", "Argentina", "American Samoa", "Austria", "Australia",
			"Aruba", "Åland Islands", "Azerbaijan", "Bosnia and Herzegovina",
			"Barbados", "Bangladesh", "Belgium", "Burkina Faso", "Bulgaria",
			"Bahrain", "Burundi", "Benin", "Saint Barthélemy", "Bermuda",
			"Brunei", "Bolivia", "Bonaire, Sint Eustatius and Saba", "Brazil",
			"Bahamas", "Bhutan", "Bouvet Island", "Botswana", "Belarus",
			"Belize", "Canada", "Cocos Islands",
			"The Democratic Republic Of Congo", "Central African Republic",
			"Congo", "Switzerland", "Côte d'Ivoire", "Cook Islands", "Chile",
			"Cameroon", "China", "Colombia", "Costa Rica", "Cuba", "Cape Verde",
			"Curaçao", "Christmas Island", "Cyprus", "Czech Republic",
			"Germany", "Djibouti", "Denmark", "Dominica", "Dominican Republic",
			"Algeria", "Ecuador", "Estonia", "Egypt", "Western Sahara",
			"Eritrea", "Spain", "Ethiopia", "Finland", "Fiji",
			"Falkland Islands", "Micronesia", "Faroe Islands", "France",
			"Gabon", "United Kingdom", "Grenada", "Georgia", "French Guiana",
			"Guernsey", "Ghana", "Gibraltar", "Greenland", "Gambia", "Guinea",
			"Guadeloupe", "Equatorial Guinea", "Greece",
			"South Georgia And The South Sandwich Islands", "Guatemala", "Guam",
			"Guinea-Bissau", "Guyana", "Hong Kong",
			"Heard Island And McDonald Islands", "Honduras", "Croatia", "Haiti",
			"Hungary", "Indonesia", "Ireland", "Israel", "Isle Of Man", "India",
			"British Indian Ocean Territory", "Iraq", "Iran", "Iceland",
			"Italy", "Jersey", "Jamaica", "Jordan", "Japan", "Kenya",
			"Kyrgyzstan", "Cambodia", "Kiribati", "Comoros",
			"Saint Kitts And Nevis", "North Korea", "South Korea", "Kuwait",
			"Cayman Islands", "Kazakhstan", "Laos", "Lebanon", "Saint Lucia",
			"Liechtenstein", "Sri Lanka", "Liberia", "Lesotho", "Lithuania",
			"Luxembourg", "Latvia", "Libya", "Morocco", "Monaco", "Moldova",
			"Montenegro", "Saint Martin", "Madagascar", "Marshall Islands",
			"Macedonia", "Mali", "Myanmar", "Mongolia", "Macao",
			"Northern Mariana Islands", "Martinique", "Mauritania",
			"Montserrat", "Malta", "Mauritius", "Maldives", "Malawi", "Mexico",
			"Malaysia", "Mozambique", "Namibia", "New Caledonia", "Niger",
			"Norfolk Island", "Nigeria", "Nicaragua", "Netherlands", "Norway",
			"Nepal", "Nauru", "Niue", "New Zealand", "Oman", "Panama", "Peru",
			"French Polynesia", "Papua New Guinea", "Philippines", "Pakistan",
			"Poland", "Saint Pierre And Miquelon", "Pitcairn", "Puerto Rico",
			"Palestine", "Portugal", "Palau", "Paraguay", "Qatar", "Reunion",
			"Romania", "Serbia", "Russia", "Rwanda", "Saudi Arabia",
			"Solomon Islands", "Seychelles", "Sudan", "Sweden", "Singapore",
			"Saint Helena", "Slovenia", "Svalbard And Jan Mayen", "Slovakia",
			"Sierra Leone", "San Marino", "Senegal", "Somalia", "Suriname",
			"South Sudan", "Sao Tome And Principe", "El Salvador",
			"Sint Maarten (Dutch part)", "Syria", "Swaziland",
			"Turks And Caicos Islands", "Chad", "French Southern Territories",
			"Togo", "Thailand", "Tajikistan", "Tokelau", "Timor-Leste",
			"Turkmenistan", "Tunisia", "Tonga", "Turkey", "Trinidad and Tobago",
			"Tuvalu", "Taiwan", "Tanzania", "Ukraine", "Uganda",
			"United States Minor Outlying Islands", "United States", "Uruguay",
			"Uzbekistan", "Vatican", "Saint Vincent And The Grenadines",
			"Venezuela", "British Virgin Islands", "U.S. Virgin Islands",
			"Vietnam", "Vanuatu", "Wallis And Futuna", "Samoa", "Yemen",
			"Mayotte", "South Africa", "Zambia", "Zimbabwe");

	@Test
	public void countryExistsRecognizesAllExistingCountries() {
		for (String knownCountry : this.knownCountries) {
			assertThat(Countries.countryExists(knownCountry), is(true));
		}
	}

	@Test
	public void countryExistsOnNonExistingCountry() {
		assertThat(Countries.countryExists("soviet union"), is(false));
	}

	@Test
	public void countryExistsCaseInsensitive() {
		// should be case
		assertThat(Countries.countryExists("sweden"), is(true));
		assertThat(Countries.countryExists("Sweden"), is(true));

		assertThat(Countries.countryExists("united STATES"), is(true));
		assertThat(Countries.countryExists("united STATES"), is(true));

		assertThat(Countries.countryExists("the democratic Republic of Congo"),
				is(true));

	}
}
