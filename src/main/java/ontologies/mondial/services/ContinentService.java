package ontologies.mondial.services;


import ontologies.mondial.dao.Continent;

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
public class ContinentService {

	private static ContinentService instance;

	public static ContinentService createDemoService() {
		if (instance == null) {

			final ContinentService continentService = new ContinentService();

			String sparqlQuery = "PREFIX : <http://www.example.org/monidal.owl#> \n"
					+ "select distinct ?name ?area where {"
					+ " ?co :continentName ?name. ?co :continentArea ?area. }";

			QuestOWLE quest = new QuestOWLE(sparqlQuery);
			try {
				quest.runQuery();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (ArrayList<String> row : quest.getQueryResult()) {
				Continent country = new Continent();
				country.setName(row.get(0));
				country.setArea(Double.parseDouble(row.get(1)));

				continentService.save(country);
			}
			instance = continentService;
		}

		return instance;
	}

	private HashMap<Long, Continent> contacts = new HashMap<>();
	private long nextId = 0;

	public synchronized List<Continent> findAll(String stringFilter) {
		ArrayList<Continent> arrayList = new ArrayList<Continent>();
		for (Continent contact : contacts.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter
						.isEmpty())
						|| contact.toString().toLowerCase()
								.contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(ContinentService.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Continent>() {

			@Override
			public int compare(Continent o1, Continent o2) {
				return (int) (o2.getId() - o1.getId());
			}
		});
		return arrayList;
	}

	public synchronized long count() {
		return contacts.size();
	}

	public synchronized void delete(Continent value) {
		contacts.remove(value.getId());
	}

	public synchronized void save(Continent entry) {
		if (entry.getId() == null) {
			entry.setId(nextId++);
		}
		try {
			entry = (Continent) BeanUtils.cloneBean(entry);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		contacts.put(entry.getId(), entry);
	}

}
