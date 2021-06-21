import {Component, OnInit} from '@angular/core';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';
import {RiderService} from '../../services/rider/rider.service';
import {Rider} from '../../models/rider';
import Chart from 'chart.js';
import {PurchaseService} from '../../services/purchase/purchase.service';

@Component({
  selector: 'app-riders',
  templateUrl: './riders.component.html',
  styleUrls: ['./riders.component.css']
})
export class RidersComponent implements OnInit {
  starIcon = faStar;
  halfStarIcon = faStarHalfAlt;
  riders: Rider[] = [];
  totalItems = 0;
  totalPages = 0;
  currentPage = 1;
  inProgress = 0;
  avgTime = null;
  avgRating: Number = null;
  math = Math;
  public ctx;
  public canvas: any;
  public chartLocations;
  locationsChartData: number[];
  locationsChartLabel: String[];

  constructor(private riderService: RiderService, private purchaseService: PurchaseService) {
  }

  ngOnInit(): void {
    this.getRiders();
    this.getRiderStats();
    this.getLocationsData();
  }


  getRiders() {
    this.riderService.getRiders(this.currentPage - 1)
      .subscribe(
        data => {
          this.riders = data['riders'];
          this.totalItems = data['totalItems'];
          this.totalPages = data['totalPages'];
        });
  }

  getRiderStats() {
    this.riderService.getRiderManagerStats()
      .subscribe(
        data => {
          this.inProgress = data['inProcess'];
          this.avgTime = data['avgTimes'];
          this.avgRating = data['avgReviews'];
        });
  }

  millisToMinutesAndSeconds(millis: number) {
    const minutes = Math.floor(millis / 60000);
    const seconds: number = Number(((millis % 60000) / 1000).toFixed(0));
    return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
  }

  getPage(event) {
    this.currentPage = event;
    this.getRiders();
    this.getRiderStats();
  }

  getLocationsData() {
    this.locationsChartData = [];
    this.locationsChartLabel = [];
    this.purchaseService.getMostDeliveredCities().subscribe(data => {
      Object.keys(data).map((key) => {
        this.locationsChartLabel.push(key);
        this.locationsChartData.push(data[key]);
      });
      this.buildChart();
    });
  }


  buildChart() {
    this.canvas = document.getElementById('chartLocations');
    this.ctx = this.canvas.getContext('2d');
    this.chartLocations = new Chart(this.ctx, {
      type: 'pie',
      data: {
        labels: this.locationsChartLabel,
        datasets: [{
          label: 'Most Delivered Cities',
          pointRadius: 0,
          pointHoverRadius: 0,
          backgroundColor: [
            '#101016',
            '#E64D2E',
            '#5E71E4',
            '#fcc468',
            '#ef8157',

          ],
          borderWidth: 0,
          data: this.locationsChartData
        }]
      },

      options: {

        legend: {
          display: true
        },

        pieceLabel: {
          render: 'percentage',
          fontColor: ['white'],
          precision: 2
        },

        tooltips: {
          enabled: true
        },

        scales: {
          yAxes: [{

            ticks: {
              display: false
            },
            gridLines: {
              drawBorder: true,
              zeroLineColor: 'transparent',
              color: 'rgba(255,255,255,0.05)'
            }

          }],

          xAxes: [{
            barPercentage: 1.6,
            gridLines: {
              drawBorder: false,
              color: 'rgba(255,255,255,0.1)',
              zeroLineColor: 'transparent'
            },
            ticks: {
              display: false,
            }
          }]
        },
      }
    });
  }

}
