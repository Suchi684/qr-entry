package com.example.demo.qr.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.qr.Entity.Member;
import com.example.demo.qr.Repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> listAll() {
        return memberRepository.findAll();
    }

    public Member findMemberById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID is required");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID format");
        }
        return memberRepository.findById(uuid)
            .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    }

    public Member findByName(String name) {
        String normalizedName = normalize(name);
        if (normalizedName == null) {
            throw new IllegalArgumentException("Name is required");
        }
        return memberRepository.findByNameIgnoreCase(normalizedName)
            .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    }

    public Member create(String name) {
        String normalizedName = normalize(name);
        if (normalizedName == null) {
            throw new IllegalArgumentException("Name is required");
        }
        if (memberRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new MemberAlreadyExistsException("Member already exists");
        }

        Member member = new Member();
        member.setName(normalizedName);
        member.setEntryScanned(false);
        member.setFoodScanned(false);
        member.setGiftScanned(false);
        return memberRepository.save(member);
    }

    @Transactional
    public ImportResult bulkImport(List<String> names) {
        if (names == null) {
            return new ImportResult(0, 0);
        }
        int inserted = 0;
        int skipped = 0;

        for (String candidateName : names) {
            String normalizedName = normalize(candidateName);
            if (normalizedName == null) {
                skipped++;
                continue;
            }
            if (memberRepository.existsByNameIgnoreCase(normalizedName)) {
                skipped++;
                continue;
            }
            Member member = new Member();
            member.setName(normalizedName);
            member.setEntryScanned(false);
            member.setFoodScanned(false);
            member.setGiftScanned(false);
            memberRepository.save(member);
            inserted++;
        }
        return new ImportResult(inserted, skipped);
    }

    @Transactional
    public ScanResult markScan(String name, String mode) {
        String normalizedName = normalize(name);
        String normalizedMode = normalizeMode(mode);
        if (normalizedName == null || normalizedMode == null) {
            throw new IllegalArgumentException("Name and valid mode are required");
        }

        Member member = memberRepository.findByNameIgnoreCase(normalizedName).orElse(null);
        if (member == null) {
            throw new MemberNotFoundException("Member not found");
        }

        boolean alreadyScanned;
        if ("entry".equals(normalizedMode)) {
            alreadyScanned = member.isEntryScanned();
            member.setEntryScanned(true);
        } else if ("food".equals(normalizedMode)) {
            alreadyScanned = member.isFoodScanned();
            member.setFoodScanned(true);
        } else {
            alreadyScanned = member.isGiftScanned();
            member.setGiftScanned(true);
        }

        memberRepository.save(member);
        return new ScanResult(member, alreadyScanned);
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeMode(String raw) {
        String cleaned = normalize(raw);
        if (cleaned == null) {
            return null;
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        if ("entry".equals(lower) || "food".equals(lower) || "gift".equals(lower)) {
            return lower;
        }
        return null;
    }

    public record ImportResult(int inserted, int skipped) { }

    public record ScanResult(Member member, boolean alreadyScanned) { }

    public static class MemberAlreadyExistsException extends RuntimeException {
        public MemberAlreadyExistsException(String message) { super(message); }
    }

    public static class MemberNotFoundException extends RuntimeException {
        public MemberNotFoundException(String message) { super(message); }
    }


}
