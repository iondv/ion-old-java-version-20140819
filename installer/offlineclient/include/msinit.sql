GO
/****** Object:  Table [dbo].[authority]    Script Date: 04.11.2014 16:38:54 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[authority](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[version] [bigint] NOT NULL,
	[authority] [varchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[user_authority]    Script Date: 04.11.2014 16:38:55 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user_authority](
	[authority_id] [bigint] NOT NULL,
	[user_id] [bigint] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[authority_id] ASC,
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[users]    Script Date: 04.11.2014 16:38:55 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[users](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[version] [bigint] NOT NULL,
	[account_expired] [bit] NOT NULL,
	[account_locked] [bit] NOT NULL,
	[enabled] [bit] NOT NULL,
	[password] [varchar](255) NOT NULL,
	[password_expired] [bit] NOT NULL,
	[username] [varchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
SET IDENTITY_INSERT [dbo].[authority] ON 

INSERT [dbo].[authority] ([id], [version], [authority]) VALUES (1, 0, N'ROLE_ADMIN')
INSERT [dbo].[authority] ([id], [version], [authority]) VALUES (2, 0, N'ROLE_USER')
SET IDENTITY_INSERT [dbo].[authority] OFF
INSERT [dbo].[user_authority] ([authority_id], [user_id]) VALUES (1, 2)
INSERT [dbo].[user_authority] ([authority_id], [user_id]) VALUES (2, 1)
INSERT [dbo].[user_authority] ([authority_id], [user_id]) VALUES (2, 2)
SET IDENTITY_INSERT [dbo].[users] ON 

INSERT [dbo].[users] ([id], [version], [account_expired], [account_locked], [enabled], [password], [password_expired], [username]) VALUES (1, 0, 0, 0, 1, N'$2a$10$fH2jRV7SgMSfImoWU2wCgO6yONndSnFUZ6Jo3moGUrOD6Mdrjvoce', 0, N'user')
INSERT [dbo].[users] ([id], [version], [account_expired], [account_locked], [enabled], [password], [password_expired], [username]) VALUES (2, 0, 0, 0, 1, N'$2a$10$hh9NdLxEz4j8vIqwyHDPDeqjwi9vPi3s3vkk8BIvDkP5k4d56pZE.', 0, N'admin')
SET IDENTITY_INSERT [dbo].[users] OFF
SET ANSI_PADDING ON

GO
/****** Object:  Index [UQ__authorit__97DB7441C607BAC9]    Script Date: 04.11.2014 16:38:55 ******/
ALTER TABLE [dbo].[authority] ADD UNIQUE NONCLUSTERED 
(
	[authority] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
SET ANSI_PADDING ON

GO
/****** Object:  Index [UQ__users__F3DBC572F151BD60]    Script Date: 04.11.2014 16:38:55 ******/
ALTER TABLE [dbo].[users] ADD UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[user_authority]  WITH CHECK ADD  CONSTRAINT [FKB55BEBCF347C1AF7] FOREIGN KEY([authority_id])
REFERENCES [dbo].[authority] ([id])
GO
ALTER TABLE [dbo].[user_authority] CHECK CONSTRAINT [FKB55BEBCF347C1AF7]
GO
ALTER TABLE [dbo].[user_authority]  WITH CHECK ADD  CONSTRAINT [FKB55BEBCF6BB5679D] FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([id])
GO
ALTER TABLE [dbo].[user_authority] CHECK CONSTRAINT [FKB55BEBCF6BB5679D]
GO
