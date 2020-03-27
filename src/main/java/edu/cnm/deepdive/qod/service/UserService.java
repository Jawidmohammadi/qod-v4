package edu.cnm.deepdive.qod.service;

import edu.cnm.deepdive.qod.model.entity.User;
import edu.cnm.deepdive.qod.model.entity.User.Role;
import edu.cnm.deepdive.qod.model.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getOrCreate(String oauthKey, String displayName) {
    return userRepository.findByOauthKey(oauthKey)
        .orElseGet(() -> {
          User user = new User();
          user.setOauthKey(oauthKey);
          user.setDisplayName(displayName);
          return userRepository.save(user);
        });
  }
  public Optional<User> get(UUID id) {
    return userRepository.findById(id);
  }

}
