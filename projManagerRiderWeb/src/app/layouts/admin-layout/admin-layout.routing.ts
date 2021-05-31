import {Routes} from '@angular/router';

import {UserProfileComponent} from '../../pages/user-profile/user-profile.component';
import {StoresComponent} from '../../pages/stores/stores.component';
import {RidersComponent} from '../../pages/riders/riders.component';

export const AdminLayoutRoutes: Routes = [
  {path: 'user-profile', component: UserProfileComponent},
  {path: 'stores', component: StoresComponent},
  {path: 'riders', component: RidersComponent}
];
