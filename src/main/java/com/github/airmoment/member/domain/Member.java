package com.github.airmoment.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	private static final String DEFAULT_PHOTO =
		"https://www.logoyogo.com/web/wp-content/uploads/edd/2021/03/logoyogo-1-164.jpg";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String profileImage;

	public static Member of(String email, String password) {
		Member member = new Member();
		member.email = email;
		member.password = password;
		member.profileImage = DEFAULT_PHOTO;
		return member;
	}
}
