package com.kh.spring.common.scheduling;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.BoardType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileDeleteTask {
	/*
     * 파일 삭제 스케쥴러
     *  - 목표 : DB에는 존재하지 않으나 WEB-SERVER상에 존재하는 쓸모없는 파일을 삭제.
     * 업무로직
     * 1. 데이터베이스(board_img 테이블)에 등록된 모든 이미지 파일 경로 목록을 조회
     * 2. 모든 게시판 유형(boardType)을 조회하여, 각각의 게시판 디렉토리 경로를 탐색
     * 3. 해당 디렉토리에서 실제 서버에 존재하는 이미지 파일 목록 을 수집
     * 4. 각 파일이 DB에 등록되어 있는 파일인지 여부를 판단
     * 5. DB에 없는 파일(즉, 더 이상 사용되지 않는 파일)이라면 삭제 처리
     * 6. 유저활동량이 적은 매달 1일 4시에 실행되도록 설정
     * 
     * 
     * ===>주말과제
     */
	
	@Autowired
	private BoardService service;
	
	@Autowired
	private ServletContext appliction;
	
//	@Scheduled(cron="0 0 4 1 * ?")
	@Scheduled(cron="1/1 * * * * *")
	public void deleteFile() {
		/*업무로직
	     * 1. 데이터베이스(board_img 테이블)에 등록된 모든 이미지 파일 경로 목록을 조회
	     * 2. 모든 게시판 유형(boardType)을 조회하여, 각각의 게시판 디렉토리 경로를 탐색
	     * 3. 해당 디렉토리에서 실제 서버에 존재하는 이미지 파일 목록 을 수집
	     * 4. 각 파일이 DB에 등록되어 있는 파일인지 여부를 판단
	     * 5. DB에 없는 파일(즉, 더 이상 사용되지 않는 파일)이라면 삭제 처리
	     * 6. 유저활동량이 적은 매달 1일 4시에 실행되도록 설정
	     */
		//1. 데이터베이스안의 모든 파일목록 조회
		List<String> list = service.selectFileList();
		
		//2. 모든게시판 유형의 디렉토리 경로 탐색
		List<BoardType> typeList = service.selectBoardTypeList();
		for(BoardType type : typeList) {
			File path = new File(appliction.getRealPath("/resources/images/board/" + type.getBoardCd()));
			if(!path.exists()) {
				continue;
			}
			File[] files = path.listFiles();
			List<File> fileList = Arrays.asList(files);
			
			 if(!list.isEmpty()) {
				 if(!fileList.isEmpty()) {
					 for(File serverFile: fileList) {
						 String fileName = serverFile.getName();
						 fileName = "/resources/images/board/"+type.getBoardCd()+"/"+fileName;
						 
						 if(list.indexOf(fileName) == -1) {
							 log.debug(fileName+"삭제완료");
							 serverFile.delete();
						 }
					 }
				 }
			 }
		}
		
	}

}
