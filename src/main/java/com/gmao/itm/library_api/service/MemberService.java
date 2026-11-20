package com.gmao.itm.library_api.service;

import com.gmao.itm.library_api.dto.MemberRequest;
import com.gmao.itm.library_api.dto.MemberResponse;
import com.gmao.itm.library_api.exception.BusinessRuleException;
import com.gmao.itm.library_api.exception.ResourceNotFoundException;
import com.gmao.itm.library_api.mapper.MemberMapper;
import com.gmao.itm.library_api.model.Member;
import com.gmao.itm.library_api.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toResponse)
                .toList();
    }

    public MemberResponse findById(Long id) {
        return memberMapper.toResponse(getMemberOrThrow(id));
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (memberRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessRuleException("A member with email '" + normalizedEmail + "' already exists");
        }

        Member member = memberMapper.toEntity(request);
        member.setEmail(normalizedEmail);
        Member saved = memberRepository.save(member);
        return memberMapper.toResponse(saved);
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMemberOrThrow(id);
        String normalizedEmail = request.email().trim().toLowerCase();

        memberRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A member with email '" + normalizedEmail + "' already exists");
                });

        memberMapper.updateEntityFromRequest(request, member);
        member.setEmail(normalizedEmail);
        Member updated = memberRepository.save(member);
        return memberMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Member member = getMemberOrThrow(id);
        memberRepository.delete(member);
    }

    public Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }
}