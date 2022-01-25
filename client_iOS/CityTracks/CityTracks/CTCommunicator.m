//
//  CTCommunicator.m
//  CityTracks
//
//  Created by Carlos Quintella on 1/17/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "CTCommunicator.h"

@implementation CTCommunicator

@synthesize serverName;



/* DUMP movements to server
 
 - (BOOL) dumpToServer: (NSString *) serverName trace: (NSCoordinate2d *) ...
 {
 SString *urlAsString = serverName;
 urlAsString = [urlAsString stringByAppendingString:@"?param1=First"]
 urlAsString = [urlAsString stringByAppendingString:@"&param2=Second"];
 NSURL *url = [NSURL URLWithString:urlAsString];
 NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:url]; [urlRequest setTimeoutInterval:30.0f];
 [urlRequest setHTTPMethod:@"POST"];
 NSString *body = @"bodyParam1=BodyValue1&bodyParam2=BodyValue2"; [urlRequest setHTTPBody:[body dataUsingEncoding:NSUTF8StringEncoding]];
 NSOperationQueue *queue = [[NSOperationQueue alloc] init];
 [NSURLConnection
 sendAsynchronousRequest:urlRequest queue:queue completionHandler:^(NSURLResponse *response,
 NSData *data, NSError *error) { 
 if ([data length] >0 && error == nil){
 NSString *html = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
 NSLog(@"HTML = %@", html); }
 else if ([data length] == 0 && error == nil){
 NSLog(@"Nothing was downloaded."); }
 else if (error != nil){
 NSLog(@"Error happened = %@", error);
 } }];
 
 */
@end
