package edu.cnm.deepdive.qod.model.repository;

import edu.cnm.deepdive.qod.model.entity.User;
import edu.cnm.deepdive.qod.model.entity.User.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByOauthKey(String oauthKey);

  Iterable<User> getAllByRoleOrderByDisplayNameAsc(Role role);
}
