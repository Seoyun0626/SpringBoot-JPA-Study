package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryOld {
    private final EntityManager em;


    public void save(Member member){
        em.persist(member); //Transaction이 commit 될 때 DB저장
    }

    // 단건 조회
    public Member findOne(Long id){
        return em.find(Member.class, id); // Member.class : 반환형 Type, em.find : 기본키 사용
    }

    public List<Member> findAll(){
        return em.createQuery("select  m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}
