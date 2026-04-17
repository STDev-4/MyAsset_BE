package io.api.myasset.domain.tier.dto;

import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.entity.UserTier;

public record TierMeResponse(
	String tier,
	String tierLabel,
	int point,
	String nextTier, // null이면 최고 티어
	String nextTierLabel // null이면 최고 티어
) {
	public static TierMeResponse from(User user) {
		UserTier current = user.getTier();
		UserTier next = current.next();
		return new TierMeResponse(
			current.name(),
			current.getLabel(),
			user.getPoint(),
			next != null ? next.name() : null,
			next != null ? next.getLabel() : null);
	}
}
