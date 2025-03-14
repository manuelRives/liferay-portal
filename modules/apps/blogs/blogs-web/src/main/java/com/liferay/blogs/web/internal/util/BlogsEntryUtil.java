/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.blogs.web.internal.util;

import com.liferay.asset.display.page.portlet.AssetDisplayPageFriendlyURLProvider;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayRenderResponse;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.LiferayPortletUtil;

import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Alejandro Tardín
 */
public class BlogsEntryUtil {

	public static String getDisplayTitle(
		ResourceBundle resourceBundle, BlogsEntry entry) {

		if (Validator.isNull(entry.getTitle())) {
			return LanguageUtil.get(resourceBundle, "untitled-entry");
		}

		return entry.getTitle();
	}

	public static String getViewEntryPortletURL(
		BlogsEntry entry, String currentURL, RenderResponse renderResponse,
		RenderRequest renderRequest, ThemeDisplay themeDisplay) {

		AssetDisplayPageFriendlyURLProvider
			assetDisplayPageFriendlyURLProvider = _serviceTracker.getService();

		String friendlyURL = null;

		try {
			friendlyURL = assetDisplayPageFriendlyURLProvider.getFriendlyURL(
				new InfoItemReference(
					BlogsEntry.class.getName(),
					new ClassPKInfoItemIdentifier(entry.getEntryId())),
				themeDisplay);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		if (friendlyURL != null) {
			return friendlyURL;
		}

		ObjectValuePair<String, String> objectValuePair = null;

		if (Validator.isNotNull(entry.getUrlTitle())) {
			objectValuePair = new ObjectValuePair<>(
				"urlTitle", entry.getUrlTitle());
		}
		else {
			objectValuePair = new ObjectValuePair<>(
				"entryId", String.valueOf(entry.getEntryId()));
		}

		LiferayRenderResponse liferayRenderResponse =
			(LiferayRenderResponse)LiferayPortletUtil.getLiferayPortletResponse(
				renderResponse);

		PortletURL portletURL = PortletURLBuilder.createLiferayPortletURL(
			liferayRenderResponse, PortletRequest.RENDER_PHASE
		).setMVCRenderCommandName(
			"/blogs/view_entry"
		).setRedirect(
			themeDisplay.getURLCurrent()
		).setParameter(
			objectValuePair.getKey(), objectValuePair.getValue()
		).build();

		String categoryId = ParamUtil.getString(
			renderRequest, "categoryId", null);

		if (categoryId != null) {
			portletURL.setParameter("categoryId", categoryId);
		}

		return portletURL.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(BlogsEntryUtil.class);

	private static final ServiceTracker<?, AssetDisplayPageFriendlyURLProvider>
		_serviceTracker = ServiceTrackerFactory.open(
			FrameworkUtil.getBundle(AssetDisplayPageFriendlyURLProvider.class),
			AssetDisplayPageFriendlyURLProvider.class);

}