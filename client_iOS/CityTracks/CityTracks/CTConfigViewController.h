//
//  CTConfigViewController.h
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTManager.h"

@interface CTConfigViewController : UIViewController

{}

@property (weak, nonatomic) IBOutlet UISlider *sliderReportingFrequency;
@property (weak, nonatomic) IBOutlet UISlider *sliderAccuracyLevel;



- (void)viewWillAppear:(BOOL)animated;

@end
