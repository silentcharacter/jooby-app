package com.mycompany.service;

import com.google.inject.Inject;
import com.mycompany.domain.User;
import com.typesafe.config.Config;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class UserService extends AbstractService<User>
{
	@Inject
	private Config config;

	public UserService()
	{
		super(User.class);
	}

	@Override
	public void onSave(User user) {
		if (StringUtils.isNotEmpty(user.password)) {
			if (!user.password.equals(user.passwordConfirm)) {
				throw new RuntimeException("Пароли не совпадают!");
			}
			user.password = hashPassword(user.email, user.password);
		} else if (user.id != null) {
			User saved = getById(user.id);
			if (saved != null) {
				user.password = saved.password;
			}
		}
	}

	private String hashPassword(String login, String pass) {
		return BCrypt.hashpw(login + pass + config.getString("application.secret"), BCrypt.gensalt());
	}

	public boolean validatePassword(User user, String login, String pass) {
		if (StringUtils.isEmpty(user.password)) {
			return false;
		}
		return BCrypt.checkpw(login + pass + config.getString("application.secret"), user.password);
	}

	@Override
	protected User listReaderCallback(User user)
	{
		user.password = "";
		user.passwordConfirm = "";
		return user;
	}
}
