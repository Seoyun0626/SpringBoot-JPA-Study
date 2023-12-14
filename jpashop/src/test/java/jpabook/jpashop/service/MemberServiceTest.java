package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) //Junit 실행 시 Spring과 함께 실행
@SpringBootTest //Autoriwired 사용 위해
@Transactional //있어야지 rollback 가능(테스트 에서)(DB 데이터 안남기 위해)
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;
    @Test
//    @Rollback(false) //등록 쿼리 확인 가능(insert문 확인 가능 + 실제 DB 반영됨)
    public void 회원가입() throws Exception{
        // given (주어질 때)
        Member member = new Member();
        member.setName("kim");

        // when (실행 하면)
        Long saveId = memberService.join(member);

        // then (아래 결과 값)
        em.flush(); //영속성 context를 DB에 반영쿼리문 확인 가능(쿼리 날리는것 확인 + rollback됨 -> DB반영 안됨)
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception{
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); // 예외 발생해야 함

        //then
        fail("예외가 발생해야 한다.");
    }
}