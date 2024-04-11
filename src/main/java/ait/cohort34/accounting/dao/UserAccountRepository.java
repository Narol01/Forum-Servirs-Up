package ait.cohort34.accounting.dao;

import ait.cohort34.accounting.model.UserAccount;
import org.springframework.data.repository.CrudRepository;

public interface UserAccountRepository extends CrudRepository<UserAccount, String> {
}
