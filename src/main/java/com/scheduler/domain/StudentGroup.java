package com.scheduler.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_groups")
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;
    private String createdByUsername;

    // Stores usernames of members as a comma-separated string
    private String memberUsernames;

    public StudentGroup() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String name) {
        this.groupName = name;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String u) {
        this.createdByUsername = u;
    }

    public String getMemberUsernames() {
        return memberUsernames;
    }

    public void setMemberUsernames(String m) {
        this.memberUsernames = m;
    }

    // Helper: add a member
    public void addMember(String username) {
        if (memberUsernames == null || memberUsernames.isEmpty()) {
            memberUsernames = username;
        } else if (!memberUsernames.contains(username)) {
            memberUsernames += "," + username;
        }
    }

    // Helper: get members as a list
    public List<String> getMemberList() {
        if (memberUsernames == null || memberUsernames.isEmpty())
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String s : memberUsernames.split(","))
            list.add(s.trim());
        return list;
    }

    public void removeMember(String username) {
        if (memberUsernames == null || memberUsernames.isEmpty())
            return;
        List<String> members = new ArrayList<>(getMemberList());
        members.remove(username);
        memberUsernames = String.join(",", members);
    }

}
