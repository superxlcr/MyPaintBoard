package com.app.superxlcr.mypaintboard.model;

/**
 * 用户模型
 * @author superxlcr
 *
 */
public class User {
	
	public static final int NO_ROOM_ID = -1; // 没有房间时的id
	
	private String username;
	private String password;
	private String nickname;
	private int roomId;
	
	public User(String username, String password, String nickname) {
		this.username = username;
		this.password = password;
		this.nickname = nickname;
		roomId = NO_ROOM_ID;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			return username.equals(((User)obj).getUsername());
		}
		return false;
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
	
	@Override
	public String toString() {
		return "User : " + username + " " + nickname + "\n";
	}
	
}
