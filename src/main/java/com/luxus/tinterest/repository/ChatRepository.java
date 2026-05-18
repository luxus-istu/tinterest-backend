package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.Chat;
import com.luxus.tinterest.entity.ChatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @EntityGraph(attributePaths = {"createdBy", "members", "members.user"})
    List<Chat> findDistinctByMembers_User_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"createdBy", "members", "members.user"})
    @Query("select c from Chat c where c.id = :id")
    Optional<Chat> findWithMembersById(@Param("id") Long id);

    @Query("""
            select c from Chat c
            where c.type = :type
              and exists (
                  select 1 from ChatMember firstMember
                  where firstMember.chat = c and firstMember.user.id = :firstUserId
              )
              and exists (
                  select 1 from ChatMember secondMember
                  where secondMember.chat = c and secondMember.user.id = :secondUserId
              )
              and (
                  select count(member)
                  from ChatMember member
                  where member.chat = c
              ) = 2
            """)
    Optional<Chat> findDirectChatBetween(@Param("type") ChatType type,
                                         @Param("firstUserId") Long firstUserId,
                                         @Param("secondUserId") Long secondUserId);

    @EntityGraph(attributePaths = {"createdBy", "members", "members.user"})
    @Query("SELECT c FROM Chat c WHERE c.isPublic = true AND c.type = 'GROUP' AND NOT EXISTS (SELECT 1 FROM ChatMember cm WHERE cm.chat = c AND cm.user.id = :userId) ORDER BY c.createdAt DESC")
    Page<Chat> findDiscoverableChats(@Param("userId") Long userId, Pageable pageable);
}
