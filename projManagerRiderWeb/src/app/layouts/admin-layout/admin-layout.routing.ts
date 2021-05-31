import {Routes} from '@angular/router';

import {DashboardComponent} from '../../pages/dashboard/dashboard.component';
import {IconsComponent} from '../../pages/icons/icons.component';
import {UserProfileComponent} from '../../pages/user-profile/user-profile.component';
import {StoresComponent} from '../../pages/stores/stores.component';
import {RidersComponent} from '../../pages/riders/riders.component';

export const AdminLayoutRoutes: Routes = [
  {path: 'dashboard', component: DashboardComponent},
  {path: 'user-profile', component: UserProfileComponent},
  {path: 'stores', component: StoresComponent},
  {path: 'riders', component: RidersComponent},
  {path: 'icons', component: IconsComponent}
];
