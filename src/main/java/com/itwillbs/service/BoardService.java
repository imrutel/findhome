package com.itwillbs.service;

import java.util.List;

import com.itwillbs.domain.BoardBean;
import com.itwillbs.domain.ImageBean;
import com.itwillbs.domain.OneRoomBean;
import com.itwillbs.domain.PageBean;

public interface BoardService {

	// 추상메서드
	public void insertBoard(BoardBean bb);
	// 방등록
	public void insertRoom(OneRoomBean bb);
	// 방 이미지 등록
	public void insertRoomImags(ImageBean bean);
	
	public List<BoardBean> getBoardList(PageBean pb);
	public Integer getBoardCount();

	public BoardBean getBoard(int num);

	public BoardBean numCheck(BoardBean bb);

	public void updateBoard(BoardBean bb);

	public void deleteBoard(BoardBean bb);
}
