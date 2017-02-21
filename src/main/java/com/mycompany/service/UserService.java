package com.mycompany.service;

import com.google.inject.Inject;
import com.mycompany.domain.User;
import com.typesafe.config.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;


public class UserService extends AbstractService<User>
{
	@Inject
	private Config config;

	public UserService()
	{
		super(User.class);
	}

	@Override
	public void onSave(User user)
	{
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

	public String hashPassword(String login, String pass)
	{
		return DigestUtils.sha1Hex(login + pass + config.getString("application.secret"));
	}
}
