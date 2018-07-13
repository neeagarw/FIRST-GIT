<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes" />
	<xsl:param name="env" />
	<xsl:param name="timestamp" />
	<xsl:param name="css" />
	<xsl:param name="fullOutput" />
	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
				<meta name="viewport" content="width=device-width" />
				<style type="text/css">
					<xsl:value-of select="$css" />
				</style>
			</head>
			<body style="font: 13px Arial, FreeSans, Helvetica, sans-serif; background:=#f0f0f0; color: #505050">
				<table id="email-wrap" align="center" cellpadding="0" cellspacing="0" width="100%" height="100%"
					style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; background: #f0f0f0; color: #505050">
					<tbody>
						<tr>
							<td style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 16px">
								<div id="title-status" class="successful"
									style="font: 13px Arial, FreeSans, Helvetica, sans-serif; padding: 8px 0; background: #7098C1;">
									<table cellpadding="0" cellspacing="0" width="100%"
										style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; border-style: solid; border-width: 1px 0; table-layout: fixed; background: #326ca6; border-color: #326ca6;">
										<tbody>
											<tr>
												<td id="title-status-text"
													style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; font-size: 16px; padding: 8px; text-shadow: 1px 1px 1px #194c19; color: #fff">
													<span class="build" style="font-weight: bold">[BTDMP] [<xsl:value-of select="$env" /> environment] > SCE Update Notification</span>
												</td>
											</tr>
										</tbody>
									</table>
								</div>

								<table id="email-wrap-inner" cellpadding="0" cellspacing="0" width="100%"
									style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight:inherit; background:#fff; border: 1 px solid #bbb; color: #; border-top: 0">
									<tbody>
										<tr>
											<td style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 16px">
												<table width="100%" cellpadding="0" cellspacing="0" class="section-header"
													style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; border-bottom: 1px solid #ddd; margin-top: 16px">
													<tbody>
														<tr>
															<td style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top">
																<h3 style="font: 13px Arial, FreeSans, Helvetica, sans-serif; margin: 16px 0 0 0; font-size: 19px; margin: 0">
																	<span style="color: #326ca6; text-decoration: none">SCE Changes</span>
																</h3>
															</td>
														</tr>
													</tbody>
												</table>
												<table width="100%" cellpadding="0" cellspacing="0" class="commits"
													style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight: inherit; margin-top: 12px; margin-top: 8px">
													<thead>
														<tr>
															<th
																style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; padding-left: 8px">
																<span style="font-weight:bold; text-decoration:underline;">Property Name</span>
															</th>
															<th class="revision"
																style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																<span style="font-weight:bold; text-decoration:underline;">Action</span>
															</th>
															<th class="revision"
																style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																<span style="font-weight:bold; text-decoration:underline;">Result</span>
															</th>
															<th width="100%"
																style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																<span style="font-weight:bold; text-decoration:underline;">Change</span>
															</th>
														</tr>
													</thead>
													<tbody>
														<xsl:for-each select="dry-run-report/property-item">
															<xsl:if test="$fullOutput = 'true' or operation != 'NONE'">
																<tr>
																	<td
																		style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; padding-left: 8px">
																		<span>
																			<xsl:value-of select="name" />
																		</span>
																		<br />
																		<span style="color: #326ca6; text-decoration: none;">
																			<xsl:value-of select="comment" />
																		</span>
																	</td>
																	<td class="revision"
																		style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																		<span class="revision-id"
																			style="background: #f0f0f0; border: 1px solid #ddd; -moz-border-radius: 3px; -webkit-border-radius: 3px; border-radius: 3px; display: inline-block; font-family: monospace; line-height: 1; max-width: 5em; overflow: hidden; padding: 2px 4px; text-overflow: ellipsis; vertical-align: top; white-space: nowrap; color: #505050">
																			<xsl:value-of select="operation" />
																		</span>
																	</td>
																	<td class="revision"
																		style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																		<xsl:if test="success = 'Success'">
																			<span class="revision-id"
																				style="background: #309130; border: 1px solid #ddd; -moz-border-radius: 3px; -webkit-border-radius: 3px; border-radius: 3px; display: inline-block; font-family: monospace; line-height: 1; max-width: 5em; overflow: hidden; padding: 2px 4px; text-overflow: ellipsis; vertical-align: top; white-space: nowrap; color: #fff">
																				<xsl:value-of select="success" />
																			</span>
																		</xsl:if>
																		<xsl:if test="success != 'Success'">
																			<span class="revision-id"
																				style="background: red; border: 1px solid #ddd; -moz-border-radius: 3px; -webkit-border-radius: 3px; border-radius: 3px; display: inline-block; font-family: monospace; line-height: 1; max-width: 5em; overflow: hidden; padding: 2px 4px; text-overflow: ellipsis; vertical-align: top; white-space: nowrap; color: #fff">
																				<xsl:value-of select="success" />
																			</span>
																		</xsl:if>
																	</td>
																	<td width="100%"
																		style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 4px 0; padding-left: 4px; white-space: nowrap">
																		<code class="change-container">
																			<xsl:if test="sensitive = 'true'">
																				(value not shown)
																			</xsl:if>
																			<xsl:if test="sensitive != 'true'">
																				<xsl:variable name="raw">
																					<xsl:value-of select="html" disable-output-escaping="yes" />
																				</xsl:variable>
																				<xsl:value-of select="replace($raw, '&amp;gt;&amp;lt;', '&amp;gt;&#xa;&amp;lt;')"
																					disable-output-escaping="yes" />
																			</xsl:if>
																		</code>
																	</td>
																</tr>
															</xsl:if>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
									</tbody>
								</table>
								<table id="email-footer" cellpadding="0" cellspacing="0" width="100%"
									style="margin: 0; border: 0; border-collapse: collapse; font-size: inherit; font-style: inherit; font-variant: inherit; font-weight:inherit;">
									<tbody>
										<tr>
											<td
												style="font: 13px Arial, FreeSans, Helvetica, sans-serif; text-align: left; vertical-align: top; padding: 16px; text-align: center">
												<p style="font: 13px Arial, FreeSans, Helvetica, sans-serif; margin: 16px 0 0 0; font-size: 11px; margin: 0">
													<small style="font-size: 11px">
														Generated:
														<xsl:value-of select="$timestamp" />
													</small>
												</p>
												<p style="font: 13px Arial, FreeSans, Helvetica, sans-serif; margin: 16px 0 0 0; font-size: 11px; margin: 0">
													<small style="font-size: 11px">
														This message was generated by
														<a href="https://portal.motive.com/portal/display/BTDCM/SCE+shell+-+CLI+and+automated+configuration"
															style="color: #326ca6; text-decoration: none"> SCE Shell Deployment Tool</a>
													</small>
												</p>
											</td>
										</tr>
									</tbody>
								</table>
							</td>
						</tr>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>