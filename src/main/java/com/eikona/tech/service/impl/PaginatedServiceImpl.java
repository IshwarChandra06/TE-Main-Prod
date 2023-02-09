package com.eikona.tech.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaginatedServiceImpl<T> {
	public Specification<T> fieldSpecification(Map<String, String> searchMap) {

		Specification<T> isDeleted = (root, query, cb) -> {
			return cb.equal(root.get("isDeleted"), false);
		};

		Set<String> searchSet = searchMap.keySet();

		for (String searchKey : searchSet) {
			Object obj = searchMap.get(searchKey);
			if (searchMap.get(searchKey) instanceof String) {
				if ("id".equalsIgnoreCase(searchKey)) {
					String idStr = searchMap.get(searchKey);
					Specification<T> idSpec = (root, query, cb) -> {
						if (idStr == null || idStr.isEmpty()) {
							return cb.conjunction();
						}
						long id = Long.parseLong(idStr);
						return cb.equal(root.get("id"), id);
					};
					isDeleted = isDeleted.and(idSpec);
				} else if (searchKey.toLowerCase().contains("date")) {
					isDeleted = isDeleted.and((Specification<T>) genericDateSpecification(searchKey, searchMap.get(searchKey)));
				}else 
					isDeleted = isDeleted.and((Specification<T>) genericSpecification(searchKey, searchMap.get(searchKey)));
			} 
		}
		return Specification.where(isDeleted);
	}
	
	

	public Specification<T> genericDateSpecification(String searchField, String searchValue) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null ;
		Calendar calendar = Calendar.getInstance();
		if(null!=searchValue) {
		try {
			  date = format.parse(searchValue);
		} catch (ParseException e) {
			e.printStackTrace();
		}
			calendar.setTime(date);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		}
			return (root, query, cb) -> {
				if (searchField == null || null==searchValue || searchValue.isEmpty() ) {
					return cb.conjunction();
				}
				return cb.greaterThan(root.get(searchField), calendar.getTime());
			};
	}

	public Specification<T> genericSpecification(String searchField, String searchValue) {
		return (root, query, cb) -> {
			if (searchField == null|| searchValue==null || searchValue.isEmpty() ) {
				return cb.conjunction();
			}
			return cb.like(cb.lower(root.<String>get(searchField)), "%" + searchValue + "%");
		};
	}
	
	public Specification<T> genericSpecification(String searchField, String searchObj, String searchValue) {
		return (root, query, cb) -> {
			if (searchField == null || searchValue==null || searchValue.isEmpty() ) {
				return cb.conjunction();
			}
			return cb.like(cb.lower(root.<String>get(searchField).get(searchObj)), "%" + searchValue + "%");
		};
	}

	

	
}
