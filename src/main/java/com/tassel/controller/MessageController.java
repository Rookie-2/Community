package com.tassel.controller;

import com.tassel.entity.Message;
import com.tassel.entity.User;
import com.tassel.service.MessageService;
import com.tassel.service.UserService;
import com.tassel.util.HostHolder;
import com.tassel.util.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shuaiyin.zhang
 * @description
 * @date 2020/09/08
 */
@Controller
public class MessageController {

	@Resource
	MessageService messageService;

	@Resource
	HostHolder hostHolder;

	@Resource
	UserService userService;

	/**
	 * 私信列表
	 *
	 * @param model
	 * @param page
	 * @return
	 */
	@GetMapping("/letter/list")
	public String getLetterList(Model model, Page page) {
		User user = hostHolder.getUser();
		// 设置分页信息
		page.setLimit(5);
		page.setPath("/letter/list");
		page.setRows(messageService.selectConversationCount(user.getId()));

		// 会话列表
		List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
		List<Map<String, Object>> conversations = new ArrayList<>();
		if (conversationList != null) {
			for (Message message : conversationList) {
				Map<String, Object> map = new HashMap<>();
				map.put("conversation", message);
				map.put("letterCount", messageService.selectLetterCount(message.getConversationId()));
				map.put("unreadCount", messageService.selectLetterUnreadCount(user.getId(), message.getConversationId()));
				int targetId = user.getId().equals(message.getFromId()) ? message.getToId() : message.getFromId();
				map.put("target", userService.queryUserById(targetId));
				conversations.add(map);
			}
		}
		model.addAttribute("conversations", conversations);

		// 查询未读消息数量
		Integer letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);

		return "/site/letter";
	}

	@GetMapping("/letter/detail/{conversationId}")
	public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
		// 分页信息
		page.setLimit(5);
		page.setPath("/letter/detail/" + conversationId);
		page.setRows(messageService.selectLetterCount(conversationId));

		// 私信列表
		List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
		List<Map<String, Object>> letters = new ArrayList<>();
		if (letterList != null) {
			for (Message message : letterList) {
				Map<String, Object> map = new HashMap<>();
				map.put("letter", message);
				map.put("fromUser", userService.queryUserById(message.getFromId()));
				letters.add(map);
			}
		}
		model.addAttribute("letters", letters);

		// 私信目标
		model.addAttribute("target", getLetterTarget(conversationId));

		return "/site/letter-detail";
	}

	private User getLetterTarget(String conversationId) {
		String[] ids = conversationId.split("_");
		User user0 = userService.queryUserById(Integer.parseInt(ids[0]));
		User user1 = userService.queryUserById(Integer.parseInt(ids[1]));
		return hostHolder.getUser().equals(user0) ? user1 : user0;
	}
}
