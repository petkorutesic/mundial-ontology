package onto1.services;

import onto1.QuestOWLE;
import onto1.dao.Country;

import org.apache.commons.beanutils.BeanUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Separate Java service class. Backend implementation for the address book
 * application, with "detached entities" simulating real world DAO. Typically
 * these something that the Java EE or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class CountryService {

	private static CountryService instance;

	public static CountryService createDemoService() {
		if (instance == null) {

			final CountryService countryService = new CountryService();

			String sparqlQuery = "PREFIX : <http://www.example.org/monidal.owl#> \n"
					+ "select distinct ?country ?area ?population ?capital where {"
					+ "  ?co :countryName ?country. ?co :countryHasCapital ?ct. ?ct :cityName ?capital.  "
					+ "  ?co :countryArea ?area. ?co :countryPopulation ?population}";

			QuestOWLE quest = new QuestOWLE(sparqlQuery);
			try {
				quest.runQuery();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (ArrayList<String> row : quest.getQueryResult()) {
				Country country = new Country();
				country.setCountry(row.get(0));
				country.setArea(Float.parseFloat(row.get(1)));
				country.setPopulation(Integer.parseInt(row.get(2)));
				country.setCapital(row.get(3));

				countryService.save(country);
			}
			instance = countryService;
		}

		return instance;
	}

	private HashMap<Long, Country> contacts = new HashMap<>();
	private long nextId = 0;

	public synchronized List<Country> findAll(String stringFilter) {
		ArrayList arrayList = new ArrayList();
		for (Country contact : contacts.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter
						.isEmpty())
						|| contact.toString().toLowerCase()
								.contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(CountryService.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Country>() {

			@Override
			public int compare(Country o1, Country o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	public synchronized long count() {
		return contacts.size();
	}

	public synchronized void delete(Country value) {
		contacts.remove(value.getId());
	}

	public synchronized void save(Country entry) {
		if (entry.getId() == null) {
			entry.setId(nextId++);
		}
		try {
			entry = (Country) BeanUtils.cloneBean(entry);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		contacts.put(entry.getId(), entry);
	}

}
