package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	//pagination
	//current page
	//cotact Per page- 5
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(int userId, Pageable pageable);
	
	
//	@Query("from Contact as c where c.user.id =:userId")
//	public List<Contact> findContactsByUser(int userId);
	
	
	
	
}
