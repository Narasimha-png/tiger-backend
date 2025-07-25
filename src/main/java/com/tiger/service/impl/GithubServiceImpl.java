package com.tiger.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tiger.dto.GitStreakDTO;
import com.tiger.dto.GithubProfile;
import com.tiger.dto.GithubStreakDTO;
import com.tiger.dto.UserDTO;
import com.tiger.entity.GithubStreak;
import com.tiger.entity.User;
import com.tiger.exception.GithubException;
import com.tiger.exception.UserException;
import com.tiger.repository.GitStreakRepository;
import com.tiger.repository.UserRepository;
import com.tiger.service.GithubService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GithubServiceImpl implements GithubService {
	private WebClient github;
	private UserRepository userRepo;
	private GitStreakRepository gitStreakRepo;
	private ModelMapper modelMapper ;
	public GithubServiceImpl(@Qualifier("github") WebClient github, UserRepository userRepo,
			GitStreakRepository gitStreakRepo, ModelMapper modelMapper) {
		this.github = github;
		this.userRepo = userRepo;
		this.gitStreakRepo = gitStreakRepo;
		this.modelMapper = modelMapper ;
	}

	public Boolean isUserExists(String gmail) {
		return userRepo.findByGmail(gmail).isPresent();
	}

	@Override
	public Mono<GithubProfile> getUserProfile(String userName) throws GithubException {
		try {
			return github.get().uri("/users/" + userName).retrieve().bodyToMono(GithubProfile.class);
		} catch (Exception e) {
			throw new GithubException(e.getMessage());
		}
	}

	@Override
	public Mono<GithubProfile> getTotalUserProfile(String userName) throws GithubException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getTotalRepositories(String userName) throws UserException {
		return github.get().uri(uriBuilder -> uriBuilder.path("/users/{user}/repos").build(userName)).retrieve()
				.bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {
				}).map(repo -> (String) repo.get("full_name")).collect(Collectors.toSet()).block();
	}

	@Override
	public List<GitStreakDTO> getTodayCommits(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail)
				.orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND.value()));

		Set<String> repos = getTotalRepositories(user.getGithubProfile());
			return Flux.fromIterable(repos)
				.flatMap(fullName -> github.get()
						.uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/commits")
								.build(fullName.split("/")[0], fullName.split("/")[1]))
						.retrieve().onStatus(status -> status.value() == 409, response -> Mono.empty())
						.bodyToFlux(GitStreakDTO.class))
				.collectList().block().stream().filter(dto -> dto.getCommit() != null
						&& dto.getCommit().getAuthor() != null && dto.getCommit().getAuthor().getDate() != null)
				.filter(dto -> {
					var commitDate = java.time.OffsetDateTime.parse(dto.getCommit().getAuthor().getDate())
							.toLocalDate();
					return commitDate.isEqual(java.time.LocalDate.now(java.time.ZoneOffset.UTC));
				}).toList();
	}

	@Override
	public void updateTodaysCommits(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail)
				.orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND.value()));
		
		int todayCommits = this.getTodayCommits(gmail).size();
		GithubStreak gitStreak = new GithubStreak();
		gitStreak.setUser(user);
		gitStreak.setTodayCommits(todayCommits);
		
		gitStreakRepo.save(gitStreak);
	}

	@Override
	public List<GithubStreakDTO> getStreaks(String gmail) throws UserException {
		if (!isUserExists(gmail)) {
			throw new UserException("User not found", HttpStatus.NOT_FOUND.value());
		}

		User user = userRepo.findByGmail(gmail)
				.orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND.value()));
		int todayCommits = this.getTodayCommits(gmail).size();
		GithubStreakDTO todayCommitDto = new GithubStreakDTO() ;
		todayCommitDto.setTodayCommits(todayCommits);
		todayCommitDto.setUser(modelMapper.map(user, UserDTO.class));
		todayCommitDto.setRecordedAt(LocalDateTime.now());
		List<GithubStreak> streaks = gitStreakRepo.findByUserGmail(gmail) ;
		List<GithubStreakDTO> finalList = streaks.stream()
			    .map(streak -> {
			        GithubStreakDTO dto = modelMapper.map(streak, GithubStreakDTO.class);
			        dto.setUser(modelMapper.map(streak.getUser(), UserDTO.class));
			        return dto;
			    })
			    .collect(Collectors.toList()); 

			finalList.add(todayCommitDto);
			return finalList;

	}

}
