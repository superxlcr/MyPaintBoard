package com.app.superxlcr.mypaintboard.model;

/**
 * 用户模型
 * 
 * @author superxlcr
 *
 */
public class User {

	public static final int DUMMY_ID = -1; // 无用的id

	private int id;
	private String username;
	private String password;
	private String nickname;
	private int roomId;
	private long loginTime;

	public User(int id, String username, String password, String nickname) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.nickname = nickname;
		roomId = DUMMY_ID;
		this.loginTime = 0;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			return id == (((User) obj).getId());
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	@Override
	public String toString() {
		return "User : " + username + " " + nickname;
	}

}
