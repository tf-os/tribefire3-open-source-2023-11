# Creating Push Requests in Control Center

You can easily evaluate, create and save push requests in Control Center.

## General

Push requests allow you to send service requests to web-based clients over a Websocket endpoint. Each push request contains a service request which is triggered in the client the push request was sent to.
> For more information, see [Push Requests](asset://tribefire.cortex.documentation:concepts-doc/features/push_requests.md).

Using Control Center, you can evaluate and save push requests for later use.

As of now, only the `Notify` service request is available for use with push requests. This service request can have different commands which provide different functionalities. All the types you can use as commands are grouped in the `com.braintribe.model.uicommand` package.

## Evaluating Push Requests

Evaluating a push request means sending a push request without saving it. This is especially useful when you need to send a single push request. 

To evaluate a push request:

1. In Control Center, use the **Quick Access** search box to open the `PushRequest` service request. 
2. In the new **PushRequest** tab, inspect the parameters: 

[](asset://tribefire.cortex.documentation:includes-doc/push_request_details.md?INCLUDE)

> If a pattern isn't specified, all registered clients match, which means if you don't specify any of the above options, all registered clients will receive the push request.
3. In this example, we want to send a notification to every user in every client connected to your tribefire instance so we leave the `clientIdPattern`, `rolePattern`, and `sessionIdPattern` fields empty.
4. In the `serviceRequest` field, click the **Assign** link. A search box opens.
5. In the search box, look for the `Notify` type, select it, and click **Finish**. A new window opens.
6. In the new window, in the **notifications** section, click the **Add** link. A search box opens.
7. In the search box, select the **MessageNotification** type and click **Add and Finish**. A configuration screen of the `MessageNotification` type opens.
8. In the configuration screen, mark the **confirmationRequired** checkbox (this means the notification displayed cannot be closed without clicking a button). In the **level** field, select **SUCCESS**, and in the **message** field, provide the message you want to have displayed, for example: **This is my first push request!**. Once done, click **Apply** on the remaining screens until you see the **PushRequest** tab again.
9. In the **PushRequest** tab (which has the **serviceRequest** field populated now), click **Evaluate**. Your push request with the attached `Notify` service request is sent and evaluated:
![](../images/push_request_notification.png)

## Saving Push Requests

You can create a push request and save it so that you can evaluate it later without having to configure it from scratch. 

To create and save a push request:

1. In Control Center, use the **Quick Access** search box to open the `PushRequest` entity type. 
2. In the new **PushRequest** tab, click **New**, select **PushRequest**, and click **OK**. A push request configuration window appears.
3. In the configuration window, inspect the parameters: 
[](asset://tribefire.cortex.documentation:includes-doc/push_request_details.md?INCLUDE)
> If a pattern isn't specified, all registered clients match, which means if you don't specify any of the above options, all registered clients will receive the push request. Also, you can explicitly add a `sessionId` and a list of metadata to your request when you create if using the `PushRequest` type.
4. In this example, we want to send a notification to every user in every client connected to your tribefire instance so we leave the `clientIdPattern`, `rolePattern`, `sessionId, `and `sessionIdPattern` fields empty.
5. In the `serviceRequest` field, click the **Assign** link. A search box opens.
6. In the search box, look for the `Notify` type, select it, and click **Finish**. A new window opens.
7. In the new window, in the **notifications** section, click the **Add** link. A search box opens.
8. In the search box, select the **MessageWithCommand** type and click **Add and Finish**. A configuration screen of the `MessageWithCommand` type opens.
9. In the configuration screen, mark the **confirmationRequired** checkbox (this means the notification displayed cannot be closed without clicking a button). In the **command** section, click the **Assign** link. A search box opens. 
10. In the search box, select the **GoToUrl** typs from the list and click **Finish**. A command configuration screen is displayed.
11. In the command configuration screen, paste the `https://academy.tribefire.com/` link in the **url** field and click **Apply**. 
12. In the **level** field, select **INFO**, and in the **message** field, provide the message you want to have displayed, for example: **Check out our awesome tutorials!**. Once done, click **Apply** on the remaining screens until you see the **PushRequest** tab again.
13. In the **PushRequest** tab, click **Commit**, then right-click your push request and select **Execute Service**. Your push request is sent and evaluated:
![](../images/push_request_notification2.png)
When you click **OK**, the `https://academy.tribefire.com/` link is opened in a new tab.
