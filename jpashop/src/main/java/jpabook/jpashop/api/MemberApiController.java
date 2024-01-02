package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     * 조회 V1 : 응답 값으로 엔티티를 직접 외부에 노출
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직 추가
     *   - 기본적으로 엔티티의 모든 값이 노출
     *   - 응답 스펙을 맞추기 위해 로직 추가 (@JsonIgnore)
     * - 엔티티 변경 -> API 스펙 변경
     * - 컬렉션을 직접 반환하면 향후 API 스펙 변경이 어려움 (별도 Result 클래스 생성으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도 DTO 반환
     */
    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        return memberService.findMembers();
    }

    /**
     * 조회 V2 : 응답 값으로 엔티티가 아닌 별도의 DTO 반환
     */
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(a -> new MemberDto(a.getName()))
                .collect(Collectors.toList());
        return new Result<>(collect); //컬렉션이 아닌 감싸기(바로 json타입으로 나가는거 방지)

    }
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data; //list
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    /**
     * 등록 V1 : 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가되는 경우
     *   - 엔티티에 API 검증을 위한 로직 추가 (@NotEmpty)
     *   - 실무에서 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어려움
     * - 엔티티 변경되면 API 스펙 변함
     *   - name -> username
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     *
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id  = memberService.join(member);
        return new CreateMemberResponse(id);

    }

    /**
     * 등촉 V2 : 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     */

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());

    }


    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    // 별도 DTO 생성
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    // 등록 후 id값 반환
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }


}
