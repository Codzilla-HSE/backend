package com.codzilla.backend.MatchRoom;

import com.codzilla.backend.MatchRoom.dto.MatchHistoryEntryDTO;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.User.User;
import com.codzilla.backend.judge.problem.ProblemService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import java.util.List;

@Controller
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;
    private final ProblemService problemService;

    public MatchController(MatchService matchService, ProblemService problemService) {
        this.matchService = matchService;
        this.problemService = problemService;
    }

    @GetMapping("/{matchId}/options")
    ResponseEntity<?> getMatchOptions(@AuthenticationPrincipal User user, @PathVariable UUID matchId) {
        var match = matchService.getMatchById(matchId);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        if (!(match.getFirstUserId().equals(user.getId()) ||
                match.getSecondUserId().equals(user.getId()))) {
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).build();
        }
        try {
            if (match.getOptions().get(Category.ProblemType).equals(ProblemType.SQL.name())) {
                var artefacts = problemService.getArtefactsOfSqlProblem(match.getProblem().getExternalId());
                var response = new MatchOptions(artefacts.getName(),
                                                artefacts.getDescription(), "SQL",
                                                "SQL",
                                                match.getOptions().get(Category.ProblemLevel));
                return ResponseEntity.ok(response);
            } else {
                var statement = problemService.getStatementOfAlgoProblem(match.getProblem().getId());
                var problem = problemService.getArtefactsOfAlgoProblem(match.getProblem().getId());
                var options = match.getOptions();
                var response = new MatchOptions(problem.getName(), statement, options.get(Category.Language), options.get(Category.ProblemType), options.get(Category.ProblemLevel));
                return ResponseEntity.ok(response);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
  
   @GetMapping("/history")
    public ResponseEntity<List<MatchHistoryEntryDTO>> getMatchHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchService.getMatchHistory(user.getId()));
    }

}

