package com.example.demo.qr.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.qr.Entity.Member;
import com.example.demo.qr.Service.MemberService;
import com.example.demo.qr.Service.MemberService.MemberAlreadyExistsException;
import com.example.demo.qr.Service.MemberService.MemberNotFoundException;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberService.listAll()
            .stream()
            .map(MemberResponse::from)
            .toList();
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String uid) {
        try {
            Member foundMember = memberService.findMemberById(uid);
            return ResponseEntity.ok(MemberResponse.from(foundMember));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateMemberRequest request) {
        try {
            Member member = memberService.create(request.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (MemberAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ImportResponse bulkImport(@RequestBody ImportRequest request) {
        MemberService.ImportResult result = memberService.bulkImport(
            request != null ? request.names() : null
        );
        return new ImportResponse(result.inserted(), result.skipped());
    }

    @PostMapping("/scan")
    public ResponseEntity<?> markScan(@RequestBody ScanRequest request) {
        try {
            MemberService.ScanResult result = memberService.markScan(request.name(), request.mode());
            return ResponseEntity.ok(new ScanResponse(MemberResponse.from(result.member()), result.alreadyScanned()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    public record CreateMemberRequest(String name) { }

    public record ImportRequest(List<String> names) { }

    public record ScanRequest(String name, String mode) { }

    public record ImportResponse(int inserted, int skipped) { }

    public record ScanResponse(MemberResponse member, boolean alreadyScanned) { }

    public record ErrorResponse(String message) { }

    public record MemberResponse(String id, String name, ScanStatus scan) {
        static MemberResponse from(Member member) {
            return new MemberResponse(
                member.getId().toString(),
                member.getName(),
                new ScanStatus(member.isEntryScanned(), member.isFoodScanned(), member.isGiftScanned())
            );
        }
    }

    public record ScanStatus(boolean entry, boolean food, boolean gift) { }
}
